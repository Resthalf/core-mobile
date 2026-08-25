package com.resthalflab.resthalfapp.app

import androidx.compose.runtime.Composable

/**
 * Controls the system status-bar icon appearance for the current screen (the app is edge-to-edge, so
 * screens draw behind a transparent status bar and pick the icon contrast themselves).
 *
 * [lightStatusBar] = true → the status bar sits over a light background, so icons render dark (the
 * default); false → icons render light, for a dark/coloured status bar. No-op where unsupported.
 */
@Composable
expect fun StatusBarAppearance(lightStatusBar: Boolean)
