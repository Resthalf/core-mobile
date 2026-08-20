package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.resthalflab.resthalfapp.core.design.RhSpacing
import org.jetbrains.compose.resources.painterResource
import resthalfapp.shared.generated.resources.Res
import resthalfapp.shared.generated.resources.resthalf_logo

/** Brand top bar: RestHalf icon on the left, an optional action (e.g. notifications) on the right. */
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
        Image(
            painter = painterResource(Res.drawable.resthalf_logo),
            contentDescription = "RestHalf",
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxWidth(0.25f),
            contentScale = ContentScale.Fit,
        )
        if (actionIcon != null) {
            IconButton(onClick = { onActionClick?.invoke() }) {
                Icon(actionIcon, contentDescription = "Notifications")
            }
        }
    }
}
