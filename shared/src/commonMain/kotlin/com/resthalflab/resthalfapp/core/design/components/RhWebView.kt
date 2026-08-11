package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Platform WebView container — Android [android.webkit.WebView], iOS WKWebView. */
@Composable
expect fun RhWebView(url: String, modifier: Modifier)
