package com.mtpnda.stuck.observability

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

enum class LogLevel(val value: String) {
    DEBUG("debug"),
    INFO("info"),
    WARNING("warning"),
    ERROR("error")
}

data class ObservabilityEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Instant = Instant.now(),
    val category: String,
    val level: LogLevel,
    val message: String,
    val metadata: Map<String, String> = emptyMap()
) {
    fun formattedLine(): String {
        return if (metadata.isEmpty()) {
            "[$category] $message"
        } else {
            val meta = metadata.entries.sortedBy { it.key }.joinToString(" ") { "${it.key}=${it.value}" }
            "[$category] $message | $meta"
        }
    }
}

/**
 * Non-blocking, zero-cloud sovereign observability sink streaming to Grafana Loki.
 * Strictly gated behind debug mode: when disabled, 0 network I/O, 0 logs buffered.
 */
class LokiObservability private constructor() {

    companion object {
        val shared = LokiObservability()
        const val DEFAULT_LOKI_URL = "http://10.0.2.2:3100/loki/api/v1/push" // 10.0.2.2 for Android emulator to host
        const val DEFAULT_TAILSCALE_URL = "https://macbook-air-valdie.baboon-gray.ts.net/arc-grafana/loki/api/v1/push"
        private const val PREFS_NAME = "stuck_observability_prefs"
        private const val KEY_ENABLED = "stuck_debug_observability_enabled"
        private const val KEY_ENDPOINT = "stuck_loki_endpoint_url"
    }

    private var prefs: SharedPreferences? = null
    private val gson = Gson()
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "stuck-observability-thread").apply { isDaemon = true }
    }

    private val buffer = mutableListOf<ObservabilityEntry>()
    private val maxBufferCount = 300

    private val isEnabledState = AtomicBoolean(false)
    private val consecutiveFailures = AtomicInteger(0)
    private val nextRetryTimeMs = AtomicLong(0)

    fun initialize(context: Context) {
        val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p
        val enabled = p.getBoolean(KEY_ENABLED, false)
        isEnabledState.set(enabled)

        executor.scheduleWithFixedDelay({
            flushOnQueue()
        }, 3, 3, TimeUnit.SECONDS)
    }

    var isEnabled: Boolean
        get() = isEnabledState.get()
        set(value) {
            isEnabledState.set(value)
            prefs?.edit()?.putBoolean(KEY_ENABLED, value)?.apply()
            if (!value) {
                executor.execute {
                    buffer.clear()
                }
            }
        }

    var endpointUrl: String
        get() = prefs?.getString(KEY_ENDPOINT, DEFAULT_LOKI_URL) ?: DEFAULT_LOKI_URL
        set(value) {
            prefs?.edit()?.putString(KEY_ENDPOINT, value)?.apply()
        }

    val isCircuitBreakerTripped: Boolean
        get() = System.currentTimeMillis() < nextRetryTimeMs.get()

    // MARK: - Logging Primitives

    fun logError(
        category: String,
        message: String,
        error: Throwable? = null,
        metadata: Map<String, String> = emptyMap()
    ) {
        val meta = metadata.toMutableMap()
        if (error != null) {
            meta["error_detail"] = error.localizedMessage ?: error.javaClass.simpleName
        }
        write(ObservabilityEntry(
            category = category,
            level = LogLevel.ERROR,
            message = message,
            metadata = meta
        ))
    }

    fun logQPS(
        action: String,
        latencyMs: Double = 0.0,
        metadata: Map<String, String> = emptyMap()
    ) {
        val meta = metadata.toMutableMap()
        meta["action"] = action
        if (latencyMs > 0) {
            meta["latency_ms"] = "%.2f".format(latencyMs)
        }
        write(ObservabilityEntry(
            category = "qps",
            level = LogLevel.INFO,
            message = "Action $action",
            metadata = meta
        ))
    }

    fun logModelInference(
        input: String,
        output: Map<String, Any?>,
        latencyMs: Double,
        metadata: Map<String, String> = emptyMap()
    ) {
        val meta = metadata.toMutableMap()
        meta["input"] = input
        meta["latency_ms"] = "%.2f".format(latencyMs)

        val outputJson = try {
            gson.toJson(output)
        } catch (e: Exception) {
            output.toString()
        }

        write(ObservabilityEntry(
            category = "inference",
            level = LogLevel.INFO,
            message = "Model inference: in='$input' -> out=$outputJson",
            metadata = meta
        ))
    }

    fun logLifecycle(
        event: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        write(ObservabilityEntry(
            category = "lifecycle",
            level = LogLevel.INFO,
            message = "Lifecycle: $event",
            metadata = metadata
        ))
    }

    // MARK: - Internal Non-Blocking Dispatch

    private fun write(entry: ObservabilityEntry) {
        // Fast-path guard 1: Sovereign Zero-Cloud Invariant.
        if (!isEnabled) return

        // Fast-path guard 2: Circuit breaker check.
        if (isCircuitBreakerTripped) return

        executor.execute {
            if (!isEnabled) return@execute
            buffer.add(entry)
            if (buffer.size > maxBufferCount) {
                buffer.subList(0, buffer.size - maxBufferCount).clear()
            }
            if (entry.level == LogLevel.ERROR || buffer.size >= 30) {
                flushOnQueue()
            }
        }
    }

    fun flush() {
        executor.submit { flushOnQueue() }.get(2, TimeUnit.SECONDS)
    }

    private fun flushOnQueue() {
        if (buffer.isEmpty() || !isEnabled) return

        if (System.currentTimeMillis() < nextRetryTimeMs.get()) {
            buffer.clear()
            return
        }

        val batch = ArrayList(buffer)
        buffer.clear()

        val payload = formatLokiPayload(batch) ?: return

        try {
            val url = URL(endpointUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("User-Agent", "Stuck-Android/1.0")
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.doOutput = true

            conn.outputStream.use { os ->
                os.write(payload.toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            if (code in 200..299) {
                consecutiveFailures.set(0)
                nextRetryTimeMs.set(0)
            } else {
                tripBreaker()
            }
            conn.disconnect()
        } catch (e: Exception) {
            tripBreaker()
        }
    }

    private fun tripBreaker() {
        val fails = consecutiveFailures.incrementAndGet()
        val backoffSec = when (fails) {
            1 -> 5L
            2 -> 30L
            3 -> 60L
            else -> 300L
        }
        nextRetryTimeMs.set(System.currentTimeMillis() + (backoffSec * 1000L))
    }

    private fun formatLokiPayload(entries: List<ObservabilityEntry>): String? {
        if (entries.isEmpty()) return null

        data class StreamKey(val category: String, val level: String)

        val grouped = mutableMapOf<StreamKey, MutableList<List<String>>>()
        for (entry in entries) {
            val key = StreamKey(entry.category, entry.level.value)
            val nanos = (entry.timestamp.epochSecond * 1_000_000_000L + entry.timestamp.nano).toString()
            val line = entry.formattedLine()
            grouped.getOrPut(key) { mutableListOf() }.add(listOf(nanos, line))
        }

        val streamsList = grouped.map { (key, values) ->
            mapOf(
                "stream" to mapOf(
                    "app" to "stuck",
                    "platform" to "android",
                    "category" to key.category,
                    "level" to key.level,
                    "env" to "debug"
                ),
                "values" to values
            )
        }

        return gson.toJson(mapOf("streams" to streamsList))
    }

    fun testConnection(callback: (Boolean, String) -> Unit) {
        executor.execute {
            try {
                val testEntry = ObservabilityEntry(
                    category = "connection_test",
                    level = LogLevel.INFO,
                    message = "Test ping from Stuck Android client",
                    metadata = mapOf("test" to "true")
                )
                val payload = formatLokiPayload(listOf(testEntry)) ?: run {
                    callback(false, "Failed to serialize payload")
                    return@execute
                }

                val url = URL(endpointUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("User-Agent", "Stuck-Android/1.0")
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                conn.doOutput = true

                conn.outputStream.use { os ->
                    os.write(payload.toByteArray(Charsets.UTF_8))
                }

                val code = conn.responseCode
                conn.disconnect()
                if (code in 200..299) {
                    callback(true, "HTTP $code OK")
                } else {
                    callback(false, "HTTP $code")
                }
            } catch (e: Exception) {
                callback(false, e.localizedMessage ?: "Connection error")
            }
        }
    }
}
