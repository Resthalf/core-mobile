package com.resthalflab.resthalfapp.core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Spacing scale. Use these instead of ad-hoc dp values so layouts stay consistent. */
object RhSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

/** Corner radii used across the design system. */
object RhRadius {
    val field = RoundedCornerShape(12.dp)
    val card = RoundedCornerShape(20.dp)
    val button = RoundedCornerShape(16.dp)
    val banner = RoundedCornerShape(14.dp)
}
