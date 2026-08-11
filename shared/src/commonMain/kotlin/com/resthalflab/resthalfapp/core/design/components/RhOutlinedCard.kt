package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.resthalflab.resthalfapp.core.design.RhRadius
import com.resthalflab.resthalfapp.core.design.RhSpacing

/** Rounded, bordered container with no shadow — the outlined counterpart to [RhCard]. */
@Composable
fun RhOutlinedCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = RhSpacing.lg,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RhRadius.card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}
