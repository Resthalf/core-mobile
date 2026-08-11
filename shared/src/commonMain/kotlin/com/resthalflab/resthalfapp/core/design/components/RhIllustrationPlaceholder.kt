package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.resthalflab.resthalfapp.core.design.RhRadius

/**
 * Placeholder for hero/decorative artwork. Swap for a real asset (Compose resource or KamelImage)
 * once design hands off illustrations — call sites won't need to change.
 */
@Composable
fun RhIllustrationPlaceholder(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Hotel,
) {
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RhRadius.card,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
    }
}
