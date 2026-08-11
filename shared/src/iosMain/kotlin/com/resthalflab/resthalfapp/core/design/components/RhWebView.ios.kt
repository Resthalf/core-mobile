package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@Composable
actual fun RhWebView(url: String, modifier: Modifier) {
    UIKitView(
        modifier = modifier,
        factory = {
            val webView = WKWebView()
            NSURL.URLWithString(url)?.let { webView.loadRequest(NSURLRequest(uRL = it)) }
            webView
        },
    )
}
