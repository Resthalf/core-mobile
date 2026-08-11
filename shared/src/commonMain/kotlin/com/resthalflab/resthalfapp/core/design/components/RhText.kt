package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * The RestHalf wordmark. Defaults to "Rest" in ink + "Half" in brand color (light backgrounds).
 * Override [restColor]/[accentColor] for dark/brand backgrounds, and [style] to size it up.
 */
@Composable
fun RhWordmark(
    modifier: Modifier = Modifier,
    restColor: Color = MaterialTheme.colorScheme.onBackground,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
) {
    Text(
        modifier = modifier,
        style = style.copy(fontWeight = FontWeight.ExtraBold),
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = restColor)) { append("Rest") }
            withStyle(SpanStyle(color = accentColor)) { append("Half") }
        },
    )
}

/** Small label above a field/section, e.g. "CITY" (uppercase) or "Room Type" (title case). */
@Composable
fun RhSectionLabel(text: String, modifier: Modifier = Modifier, uppercase: Boolean = true) {
    Text(
        modifier = modifier,
        text = if (uppercase) text.uppercase() else text,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = if (uppercase) 0.8.sp else 0.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Bold section heading, e.g. "Why book with RestHalf?". */
@Composable
fun RhSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
    )
}
