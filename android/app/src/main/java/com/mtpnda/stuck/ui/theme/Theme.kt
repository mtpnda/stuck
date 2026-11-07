package com.mtpnda.stuck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CalmColorScheme = lightColorScheme(
    primary = ContextColors.Charcoal,
    onPrimary = ContextColors.OnCharcoal,
    background = ContextColors.Bg,
    surface = ContextColors.Card,
    onBackground = ContextColors.InkPrimary,
    onSurface = ContextColors.InkPrimary
)

@Composable
fun ContextsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CalmColorScheme,
        typography = Typography,
        content = content
    )
}
