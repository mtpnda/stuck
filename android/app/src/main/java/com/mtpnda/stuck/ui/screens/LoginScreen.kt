package com.mtpnda.stuck.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtpnda.stuck.data.ContextRepository
import com.mtpnda.stuck.ui.components.*
import com.mtpnda.stuck.ui.theme.ContextColors
import com.mtpnda.stuck.ui.theme.DlsScales

@Composable
fun LoginScreen(
    repository: ContextRepository,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var biometricsEnabled by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ContextColors.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // BioCue DLS Stack of Bars Crest & Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
        ) {
            StackOfBarsEmblem(size = 96.dp)

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ContextColors.InfoSoft)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "BIOCUE DLS · COGNITIVE FLOW · ZERO CLOUD",
                    style = MaterialTheme.typography.labelSmall,
                    color = ContextColors.InfoInk
                )
            }
        }

        Text(
            text = "Stuck",
            style = MaterialTheme.typography.displayMedium,
            color = ContextColors.InkPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Unstick your thoughts.\nKnow what's in your head.",
            style = MaterialTheme.typography.bodyLarge,
            color = ContextColors.InkSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Balance your cognitive load across iOS and Android. Everything stays strictly on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = ContextColors.InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Auth Actions
        PillButton(
            title = "Continue with Google",
            loading = isLoading,
            onClick = {
                repository.createProfile("Valdie Arsanur", "valdie@stuck.app", faceId = biometricsEnabled, provider = "google")
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        GhostButton(
            title = "Continue as Guest",
            onClick = {
                repository.createProfile("Guest", "guest@stuck.app", faceId = false, provider = "guest")
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Sign-In Profiles
        Text(
            text = "QUICK SIGN-IN PROFILES",
            style = MaterialTheme.typography.labelSmall,
            color = ContextColors.InkMuted,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ContextColors.SurfaceRaised)
                    .border(1.dp, ContextColors.BorderSubtle, RoundedCornerShape(999.dp))
                    .clickable { repository.createProfile("Valdie Arsanur", "valdie@stuck.app", faceId = false, provider = "preset") }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("Valdie (Owner)", style = MaterialTheme.typography.labelMedium, color = ContextColors.InkPrimary)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ContextColors.SurfaceRaised)
                    .border(1.dp, ContextColors.BorderSubtle, RoundedCornerShape(999.dp))
                    .clickable { repository.createProfile("Partner", "partner@family.com", faceId = false, provider = "preset") }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("Partner", style = MaterialTheme.typography.labelMedium, color = ContextColors.InkPrimary)
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(14.dp))
            InlineNotice(text = errorMessage!!)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Separator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = ContextColors.BorderSubtle)
            Text(
                text = " OR ENTER YOUR DETAILS ",
                style = MaterialTheme.typography.labelSmall,
                color = ContextColors.InkMuted
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = ContextColors.BorderSubtle)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Manual Inputs
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Your name", style = MaterialTheme.typography.titleMedium, color = ContextColors.InkPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            CalmTextField(value = name, onValueChange = { name = it }, placeholder = "Name")

            Spacer(modifier = Modifier.height(14.dp))

            Text("Email (for group sharing)", style = MaterialTheme.typography.titleMedium, color = ContextColors.InkPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            CalmTextField(value = email, onValueChange = { email = it }, placeholder = "you@example.com")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lock with Biometrics / Fingerprint", style = MaterialTheme.typography.bodyLarge, color = ContextColors.InkPrimary)
                Switch(
                    checked = biometricsEnabled,
                    onCheckedChange = { biometricsEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = ContextColors.Charcoal, checkedTrackColor = ContextColors.InfoSoft)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PillButton(
                title = "Get started",
                onClick = {
                    val finalName = name.trim()
                    if (finalName.isNotBlank()) {
                        repository.createProfile(finalName, email, faceId = biometricsEnabled, provider = "manual")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy Guarantee
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(DlsScales.radiusMd.dp))
                .background(ContextColors.CalmSoft)
                .border(1.dp, ContextColors.CalmChip, RoundedCornerShape(DlsScales.radiusMd.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "🔒 Local-First Privacy · Zero cloud servers, no trackers. Your thoughts stay entirely on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = ContextColors.CalmInk
            )
        }
    }
}

@Composable
fun StackOfBarsEmblem(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 96.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(DlsScales.radiusXl.dp))
            .background(ContextColors.Charcoal)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 7.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ContextColors.CalmInk)
            )
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 7.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ContextColors.SurfaceRaised)
            )
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 7.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ContextColors.Bg)
            )
            Box(
                modifier = Modifier
                    .size(width = 26.dp, height = 7.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ContextColors.AlertInk)
            )
        }
    }
}
