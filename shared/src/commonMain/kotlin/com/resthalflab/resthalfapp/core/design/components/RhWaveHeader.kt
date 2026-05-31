package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Asymmetric S-wave: the brand area is shorter on the left (white bulges up under the title) and
 * taller on the right (where the logo sits), with a smooth flowing curve between.
 */
private val WaveBottomShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(0f, 0f)
    lineTo(0f, h * 0.75f)
    cubicTo(
        w * 0.50f, h * 0.60f,   // control 1 — crest near the left
        w * 0.60f, h * 1.0f,   // control 2 — trough toward the right
        w, h * 0.85f,           // end at the right edge
    )
    lineTo(w, 0f)
    close()
}

/**
 * Brand-colored header with an S-wave bottom. Content is inset below the status bar; align children
 * with `Modifier.align(...)`. Reusable across auth / onboarding screens.
 */
@Composable
fun RhWaveHeader(
    modifier: Modifier = Modifier,
    height: Dp = 300.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth().height(height)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(WaveBottomShape)
                .background(color),
        )
        Box(
            modifier = Modifier.matchParentSize().statusBarsPadding(),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}
