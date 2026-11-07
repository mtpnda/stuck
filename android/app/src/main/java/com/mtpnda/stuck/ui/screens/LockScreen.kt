package com.mtpnda.stuck.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mtpnda.stuck.data.ContextRepository
import com.mtpnda.stuck.ui.components.EmojiAvatar
import com.mtpnda.stuck.ui.components.GhostButton
import com.mtpnda.stuck.ui.components.PillButton
import com.mtpnda.stuck.ui.components.SoftCard
import com.mtpnda.stuck.ui.theme.ContextColors

@Composable
fun LockScreen(
    repository: ContextRepository,
    modifier: Modifier = Modifier
) {
    val profile by repository.profile.collectAsState()
    var hasFailed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(ContextColors.Bg)
            .padding(24.dp)
    ) {
        SoftCard(padding = 24.dp) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EmojiAvatar(text = profile?.initials ?: "U", size = 64.dp)

                Text(
                    text = "Welcome back, ${profile?.name ?: "User"}",
                    style = MaterialTheme.typography.titleLarge,
                    color = ContextColors.InkPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (hasFailed) "Couldn't verify it's you. Try again." else "Your contexts are locked.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasFailed) ContextColors.AlertInk else ContextColors.InkMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                PillButton(
                    title = "Unlock with Biometrics",
                    onClick = {
                        repository.unlock()
                    }
                )

                GhostButton(
                    title = "Continue as Guest",
                    onClick = {
                        repository.createProfile("Guest", "guest@contexts.app", faceId = false, provider = "guest")
                    }
                )
            }
        }
    }
}
