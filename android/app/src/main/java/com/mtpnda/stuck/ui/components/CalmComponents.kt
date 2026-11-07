package com.mtpnda.stuck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtpnda.stuck.ui.theme.ContextColors
import com.mtpnda.stuck.ui.theme.DlsScales

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DlsScales.radiusCard.dp))
            .background(ContextColors.Card)
            .border(1.dp, ContextColors.BorderSubtle, RoundedCornerShape(DlsScales.radiusCard.dp))
            .padding(padding),
        content = content
    )
}

@Composable
fun PillButton(
    title: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ContextColors.Charcoal,
            contentColor = ContextColors.OnCharcoal,
            disabledContainerColor = ContextColors.BorderSubtle,
            disabledContentColor = ContextColors.InkMuted
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(DlsScales.sizeButtonHeight.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = ContextColors.OnCharcoal,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun OutlinedPillButton(
    title: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(999.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp, brush = androidx.compose.ui.graphics.SolidColor(ContextColors.BorderStrong)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = ContextColors.Card,
            contentColor = ContextColors.InkPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(DlsScales.sizeButtonHeight.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun CoralOutlinedPillButton(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp, brush = androidx.compose.ui.graphics.SolidColor(ContextColors.AlertChip)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = ContextColors.Card,
            contentColor = ContextColors.AlertInk
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(DlsScales.sizeButtonHeight.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = ContextColors.AlertInk
        )
    }
}

@Composable
fun GhostButton(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = DlsScales.sizeTouchTarget.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = ContextColors.InkSecondary
        )
    }
}

@Composable
fun StatusPill(
    label: String,
    chipColor: Color,
    softColor: Color,
    inkColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(softColor)
            .border(1.dp, chipColor.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(chipColor)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = inkColor
        )
    }
}

@Composable
fun InlineNotice(
    text: String,
    chipColor: Color = ContextColors.AlertChip,
    softColor: Color = ContextColors.AlertSoft,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DlsScales.radiusMd.dp))
            .background(softColor)
            .border(1.dp, chipColor, RoundedCornerShape(DlsScales.radiusMd.dp))
            .padding(14.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = ContextColors.InkPrimary
        )
    }
}

@Composable
fun CalmTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = ContextColors.InkMuted) },
        singleLine = singleLine,
        shape = RoundedCornerShape(DlsScales.radiusMd.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ContextColors.Card,
            unfocusedContainerColor = ContextColors.Card,
            focusedBorderColor = ContextColors.BorderStrong,
            unfocusedBorderColor = ContextColors.BorderSubtle,
            focusedTextColor = ContextColors.InkPrimary,
            unfocusedTextColor = ContextColors.InkPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
    )
}

@Composable
fun EmojiAvatar(
    text: String,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(ContextColors.SurfaceRaised)
            .border(1.dp, ContextColors.BorderSubtle, CircleShape)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.45f).sp,
            color = ContextColors.InkPrimary
        )
    }
}
