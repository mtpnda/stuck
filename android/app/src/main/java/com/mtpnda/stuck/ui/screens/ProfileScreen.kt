package com.mtpnda.stuck.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mtpnda.stuck.data.ContextRepository
import com.mtpnda.stuck.observability.LokiObservability
import com.mtpnda.stuck.ui.components.*
import com.mtpnda.stuck.ui.theme.ContextColors

@Composable
fun ProfileScreen(
    repository: ContextRepository,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by repository.profile.collectAsState()
    val limit by repository.limit.collectAsState()
    val contexts by repository.contexts.collectAsState()

    val totalMinutes = remember(contexts) {
        contexts.sumOf { it.totalTimeSpentMillis } / 1000 / 60
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ContextColors.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Profile & Settings",
                style = MaterialTheme.typography.titleLarge,
                color = ContextColors.InkPrimary
            )
            Text(
                text = "Done",
                style = MaterialTheme.typography.labelLarge,
                color = ContextColors.InkPrimary,
                modifier = Modifier.clickable { onDismiss() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Details Card
        SoftCard(padding = 20.dp) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EmojiAvatar(text = profile?.initials ?: "U", size = 54.dp)
                Column {
                    Text(
                        text = profile?.name ?: "Guest User",
                        style = MaterialTheme.typography.titleMedium,
                        color = ContextColors.InkPrimary
                    )
                    if (profile?.email != null) {
                        Text(
                            text = profile?.email!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = ContextColors.InkMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusPill(
                        label = (profile?.provider ?: "LOCAL").uppercase(),
                        chipColor = ContextColors.CalmChip,
                        softColor = ContextColors.CalmSoft,
                        inkColor = ContextColors.CalmInk
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cognitive Limit Setting
        SoftCard(padding = 20.dp) {
            Text("Cognitive Load Limit", style = MaterialTheme.typography.titleMedium, color = ContextColors.InkPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Maximum contexts you can hold simultaneously before over-limit warnings appear.",
                style = MaterialTheme.typography.bodySmall,
                color = ContextColors.InkMuted
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$limit contexts",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ContextColors.InkPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { repository.setLimit(limit - 1) },
                        colors = ButtonDefaults.buttonColors(containerColor = ContextColors.SurfaceRaised)
                    ) {
                        Text("−", color = ContextColors.InkPrimary)
                    }
                    Button(
                        onClick = { repository.setLimit(limit + 1) },
                        colors = ButtonDefaults.buttonColors(containerColor = ContextColors.SurfaceRaised)
                    ) {
                        Text("+", color = ContextColors.InkPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Focus Statistics
        SoftCard(padding = 20.dp) {
            Text("Focus Statistics", style = MaterialTheme.typography.titleMedium, color = ContextColors.InkPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$totalMinutes minutes",
                style = MaterialTheme.typography.headlineLarge,
                color = ContextColors.InfoInk
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Total accumulated focus sessions recorded across your contexts.",
                style = MaterialTheme.typography.bodySmall,
                color = ContextColors.InkMuted
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Developer Observability (Loki & Grafana)
        var isObservabilityEnabled by remember { mutableStateOf(LokiObservability.shared.isEnabled) }
        var lokiEndpointText by remember { mutableStateOf(LokiObservability.shared.endpointUrl) }
        var testStatusText by remember { mutableStateOf<String?>(null) }
        var isTesting by remember { mutableStateOf(false) }

        SoftCard(padding = 20.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Debug Observability", style = MaterialTheme.typography.titleMedium, color = ContextColors.InkPrimary)
                        Text(
                            "Push telemetry & inference logs to Grafana / Loki",
                            style = MaterialTheme.typography.bodySmall,
                            color = ContextColors.InkMuted
                        )
                    }
                    Switch(
                        checked = isObservabilityEnabled,
                        onCheckedChange = {
                            isObservabilityEnabled = it
                            LokiObservability.shared.isEnabled = it
                        }
                    )
                }

                if (isObservabilityEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loki Push Endpoint", style = MaterialTheme.typography.labelSmall, color = ContextColors.InkSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = lokiEndpointText,
                        onValueChange = {
                            lokiEndpointText = it
                            LokiObservability.shared.endpointUrl = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ContextColors.InkPrimary,
                            unfocusedTextColor = ContextColors.InkPrimary,
                            focusedBorderColor = ContextColors.InfoInk,
                            unfocusedBorderColor = ContextColors.BorderSubtle
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isTesting = true
                                testStatusText = "Connecting..."
                                LokiObservability.shared.testConnection { ok, msg ->
                                    isTesting = false
                                    testStatusText = if (ok) "✓ $msg" else "✗ $msg"
                                }
                            },
                            enabled = !isTesting,
                            colors = ButtonDefaults.buttonColors(containerColor = ContextColors.Charcoal)
                        ) {
                            Text(if (isTesting) "Testing..." else "Test Connection", color = ContextColors.OnCharcoal)
                        }

                        testStatusText?.let { st ->
                            Text(
                                text = st,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (st.startsWith("✓")) ContextColors.CalmInk else ContextColors.AlertInk
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Zero-Cloud Guarantee: Strictly off by default. Only activates when enabled above. Circuit breaker fast-drops on failure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ContextColors.InkMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedPillButton(
            title = "Sign Out",
            onClick = {
                repository.signOut()
                onDismiss()
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        CoralOutlinedPillButton(
            title = "Reset All Contexts & Data",
            onClick = {
                repository.resetAllData()
                onDismiss()
            }
        )
    }
}
