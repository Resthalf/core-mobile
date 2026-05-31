package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.resthalflab.resthalfapp.core.design.RhRadius
import com.resthalflab.resthalfapp.core.design.RhSpacing

/**
 * Filled tinted callout with a leading icon, a bold title, a supporting line, and an optional
 * muted [caption] third line (e.g. the stay window with check-in/out detail).
 */
@Composable
fun RhInfoBanner(
    title: String,
    subtitle: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RhRadius.banner,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(RhSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.padding(start = RhSpacing.md)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (caption != null) {
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                    )
                }
            }
        }
    }
}
