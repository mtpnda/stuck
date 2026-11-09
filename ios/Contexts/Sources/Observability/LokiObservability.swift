import Foundation
import SwiftUI

/// Log level for structured telemetry.
enum LogLevel: String, Codable, Sendable {
    case debug
    case info
    case warning
    case error
}

/// A single structured log entry.
struct ObservabilityEntry: Sendable {
    let id: String
    let timestamp: Date
    let category: String
    let level: LogLevel
    let message: String
    let metadata: [String: String]

    init(
        category: String,
        level: LogLevel,
        message: String,
        metadata: [String: String] = [:]
    ) {
        self.id = UUID().uuidString
        self.timestamp = Date()
        self.category = category
        self.level = level
        self.message = message
        self.metadata = metadata
    }

    func formattedLine() -> String {
        if metadata.isEmpty {
            return "[\(category)] \(message)"
        }
        let metaString = metadata.map { "\($0.key)=\($0.value)" }.sorted().joined(separator: " ")
        return "[\(category)] \(message) | \(metaString)"
    }
}

/// Non-blocking, zero-cloud sovereign observability sink streaming to Grafana Loki.
/// Strictly gated behind debug mode: when disabled, 0 network I/O, 0 logs buffered.
final class LokiObservability: @unchecked Sendable {
    static let shared = LokiObservability()

    static let defaultLokiURLString = "http://127.0.0.1:3100/loki/api/v1/push"
    static let defaultTailscaleLokiURLString = "https://macbook-air-valdie.baboon-gray.ts.net/arc-grafana/loki/api/v1/push"

    private let queue = DispatchQueue(label: "com.mtpnda.stuck.observability", qos: .utility)
    private var buffer: [ObservabilityEntry] = []
    private let maxBufferCount = 300
    private var timer: DispatchSourceTimer?
    private let session: URLSession

    // Circuit breaker state
    private let breakerLock = NSLock()
    private var consecutiveFailures = 0
    private var nextRetryTime: CFAbsoluteTime = 0

    // Configuration keys in UserDefaults
    private let kEnabledKey = "stuck_debug_observability_enabled"
    private let kEndpointKey = "stuck_loki_endpoint_url"

    private init() {
        let config = URLSessionConfiguration.ephemeral
        config.timeoutIntervalForRequest = 4.0
        config.timeoutIntervalForResource = 8.0
        self.session = URLSession(configuration: config)

        if isEnabled {
            startTimerOnQueue()
        }
    }

    deinit {
        timer?.cancel()
    }

    // MARK: - Configuration

    var isEnabled: Bool {
        get {
            UserDefaults.standard.bool(forKey: kEnabledKey)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: kEnabledKey)
            queue.async { [weak self] in
                guard let self = self else { return }
                if newValue {
                    self.startTimerOnQueue()
                } else {
                    self.stopTimerOnQueue()
                    self.buffer.removeAll(keepingCapacity: true)
                }
            }
        }
    }

    var endpointUrl: URL {
        get {
            if let saved = UserDefaults.standard.string(forKey: kEndpointKey),
               let url = URL(string: saved), !saved.isEmpty {
                return url
            }
            return URL(string: Self.defaultLokiURLString)!
        }
        set {
            UserDefaults.standard.set(newValue.absoluteString, forKey: kEndpointKey)
        }
    }

    var isCircuitBreakerTripped: Bool {
        breakerLock.lock()
        defer { breakerLock.unlock() }
        return CFAbsoluteTimeGetCurrent() < nextRetryTime
    }

    // MARK: - Logging APIs

    /// Record a plain error log with optional underlying Error and metadata.
    func logError(
        category: String,
        message: String,
        error: Error? = nil,
        metadata: [String: String] = [:]
    ) {
        var meta = metadata
        if let err = error {
            meta["error_detail"] = err.localizedDescription
        }
        let entry = ObservabilityEntry(
            category: category,
            level: .error,
            message: message,
            metadata: meta
        )
        write(entry)
    }

    /// Record action monitoring for QPS & latency calculation.
    func logQPS(
        action: String,
        latencyMs: Double = 0.0,
        metadata: [String: String] = [:]
    ) {
        var meta = metadata
        meta["action"] = action
        if latencyMs > 0 {
            meta["latency_ms"] = String(format: "%.2f", latencyMs)
        }
        let entry = ObservabilityEntry(
            category: "qps",
            level: .info,
            message: "Action \(action)",
            metadata: meta
        )
        write(entry)
    }

    /// Record model inference log: raw unstructured user input vs parsed structured output.
    func logModelInference(
        input: String,
        output: [String: Any],
        latencyMs: Double,
        metadata: [String: String] = [:]
    ) {
        var meta = metadata
        meta["input"] = input
        meta["latency_ms"] = String(format: "%.2f", latencyMs)

        var outputSummary: String = ""
        if let jsonData = try? JSONSerialization.data(withJSONObject: output, options: [.fragmentsAllowed]),
           let jsonString = String(data: jsonData, encoding: .utf8) {
            outputSummary = jsonString
        } else {
            outputSummary = String(describing: output)
        }

        let entry = ObservabilityEntry(
            category: "inference",
            level: .info,
            message: "Model inference: in='\(input)' -> out=\(outputSummary)",
            metadata: meta
        )
        write(entry)
    }

    /// Record lifecycle events (launch, resume, memory warning).
    func logLifecycle(
        event: String,
        metadata: [String: String] = [:]
    ) {
        let entry = ObservabilityEntry(
            category: "lifecycle",
            level: .info,
            message: "Lifecycle: \(event)",
            metadata: metadata
        )
        write(entry)
    }

    // MARK: - Core Non-Blocking Dispatch

    private func write(_ entry: ObservabilityEntry) {
        // Fast-path guard 1: Sovereign Zero-Cloud Invariant.
        // If debug mode is disabled, drop immediately at caller thread with zero allocations.
        guard isEnabled else { return }

        // Fast-path guard 2: Circuit breaker check.
        // If Loki is down, drop immediately with zero lock contention or queue dispatch.
        if isCircuitBreakerTripped { return }

        queue.async { [weak self] in
            guard let self = self, self.isEnabled else { return }
            if self.timer == nil {
                self.startTimerOnQueue()
            }

            self.buffer.append(entry)
            if self.buffer.count > self.maxBufferCount {
                self.buffer.removeFirst(self.buffer.count - self.maxBufferCount)
            }

            // Flush immediately on critical errors or when batch reaches 30
            if entry.level == .error || self.buffer.count >= 30 {
                self.flushOnQueue()
            }
        }
    }

    func flush() {
        queue.sync { flushOnQueue() }
    }

    // MARK: - Queue & Batch Streaming

    private func startTimerOnQueue() {
        guard timer == nil else { return }
        let t = DispatchSource.makeTimerSource(queue: queue)
        t.schedule(deadline: .now() + 3.0, repeating: 3.0, leeway: .milliseconds(500))
        t.setEventHandler { [weak self] in self?.flushOnQueue() }
        t.resume()
        self.timer = t
    }

    private func stopTimerOnQueue() {
        timer?.cancel()
        timer = nil
    }

    private func flushOnQueue() {
        guard !buffer.isEmpty, isEnabled else { return }

        let now = CFAbsoluteTimeGetCurrent()
        breakerLock.lock()
        let isTripped = now < nextRetryTime
        breakerLock.unlock()

        if isTripped {
            buffer.removeAll(keepingCapacity: true)
            return
        }

        let batch = buffer
        buffer.removeAll(keepingCapacity: true)

        guard let payload = formatLokiPayload(entries: batch) else { return }

        var request = URLRequest(url: endpointUrl)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Stuck-iOS/1.0", forHTTPHeaderField: "User-Agent")
        request.httpBody = payload
        request.timeoutInterval = 3.0

        let task = session.dataTask(with: request) { [weak self] _, response, error in
            guard let self = self else { return }
            self.breakerLock.lock()
            defer { self.breakerLock.unlock() }

            if error != nil {
                self.consecutiveFailures += 1
                let backoff: TimeInterval
                switch self.consecutiveFailures {
                case 1: backoff = 5.0
                case 2: backoff = 30.0
                case 3: backoff = 60.0
                default: backoff = 300.0
                }
                self.nextRetryTime = CFAbsoluteTimeGetCurrent() + backoff
            } else if let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) {
                self.consecutiveFailures = 0
                self.nextRetryTime = 0
            }
        }
        task.resume()
    }

    /// Formats entries into standard Grafana Loki push JSON format.
    private func formatLokiPayload(entries: [ObservabilityEntry]) -> Data? {
        guard !entries.isEmpty else { return nil }

        struct StreamKey: Hashable {
            let category: String
            let level: String
        }

        var grouped: [StreamKey: [[String]]] = [:]
        for entry in entries {
            let key = StreamKey(category: entry.category, level: entry.level.rawValue)
            let nanos = String(UInt64(max(0, entry.timestamp.timeIntervalSince1970) * 1_000_000_000))
            let line = entry.formattedLine()
            grouped[key, default: []].append([nanos, line])
        }

        var streamsList: [[String: Any]] = []
        for (key, values) in grouped {
            let streamObj: [String: Any] = [
                "stream": [
                    "app": "stuck",
                    "platform": "ios",
                    "category": key.category,
                    "level": key.level,
                    "env": "debug"
                ],
                "values": values
            ]
            streamsList.append(streamObj)
        }

        let root: [String: Any] = ["streams": streamsList]
        return try? JSONSerialization.data(withJSONObject: root, options: [])
    }

    // MARK: - Test Ping

    /// Send a one-shot probe to Loki to verify connectivity in settings.
    func testConnection(completion: @escaping (Bool, String) -> Void) {
        let testEntry = ObservabilityEntry(
            category: "connection_test",
            level: .info,
            message: "Test ping from Stuck iOS client",
            metadata: ["test": "true", "timestamp": ISO8601DateFormatter().string(from: Date())]
        )
        guard let payload = formatLokiPayload(entries: [testEntry]) else {
            completion(false, "Failed to serialize test payload")
            return
        }

        var request = URLRequest(url: endpointUrl)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Stuck-iOS/1.0", forHTTPHeaderField: "User-Agent")
        request.httpBody = payload
        request.timeoutInterval = 4.0

        let task = session.dataTask(with: request) { _, response, error in
            if let err = error {
                DispatchQueue.main.async {
                    completion(false, err.localizedDescription)
                }
                return
            }
            if let http = response as? HTTPURLResponse {
                if (200...299).contains(http.statusCode) {
                    DispatchQueue.main.async {
                        completion(true, "HTTP \(http.statusCode) OK")
                    }
                } else {
                    DispatchQueue.main.async {
                        completion(false, "HTTP \(http.statusCode)")
                    }
                }
            } else {
                DispatchQueue.main.async {
                    completion(false, "Unknown response")
                }
            }
        }
        task.resume()
    }
}
