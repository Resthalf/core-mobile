package com.resthalflab.resthalfapp.app

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun StatusBarAppearance(lightStatusBar: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, lightStatusBar) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = lightStatusBar
        onDispose {
            // Restore the light-background default (dark icons) when the screen leaves composition.
            controller?.isAppearanceLightStatusBars = true
        }
    }
}
