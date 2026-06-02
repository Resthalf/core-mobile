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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.resthalflab.resthalfapp.core.design.RhRadius
import com.resthalflab.resthalfapp.core.design.RhSpacing

/**
 * Filled tinted callout with a leading icon, a bold title, a supporting line, and an optional
 * muted [caption] third line. Colors default to the brand container; override for warning/success.
 */
@Composable
fun RhInfoBanner(
    subtitle: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    title: String? = null,
    caption: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconTint: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RhRadius.banner,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(RhSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = iconTint,
            )
            Column(modifier = Modifier.padding(start = RhSpacing.md)) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = contentColor,
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                )
                if (caption != null) {
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.75f),
                    )
                }
            }
        }
    }
}
