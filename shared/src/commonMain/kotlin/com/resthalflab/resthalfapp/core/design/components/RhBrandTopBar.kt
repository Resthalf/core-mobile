package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.resthalflab.resthalfapp.core.design.RhSpacing

/** Brand top bar: RestHalf wordmark on the left, an optional action (e.g. notifications) on the right. */
@Composable
fun RhBrandTopBar(
    modifier: Modifier = Modifier,
    actionIcon: ImageVector? = Icons.Outlined.Notifications,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RhSpacing.xl, vertical = RhSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RhWordmark()
        if (actionIcon != null) {
            IconButton(onClick = { onActionClick?.invoke() }) {
                Icon(actionIcon, contentDescription = "Notifications")
            }
        }
    }
}
