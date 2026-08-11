package com.resthalflab.resthalfapp.core.design.components

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

/** White rounded elevated container used to group content (e.g. the search form). */
@Composable
fun RhCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = RhSpacing.xl,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RhRadius.card,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}
