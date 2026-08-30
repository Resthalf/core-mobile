package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * Loads a remote image via Coil (memory + disk cached, so re-scrolling a list doesn't re-decode) and
 * draws [RhIllustrationPlaceholder] behind it — the placeholder shows through while loading or on
 * failure, then the image paints over it once ready. Single place to manage image loading; the
 * cached [coil3.ImageLoader] is configured once in App(). Uses a plain (non-subcompose) AsyncImage so
 * it stays measurable under `Modifier.height(IntrinsicSize.Min)`.
 */
@Composable
fun RhRemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    Box(modifier = modifier) {
        RhIllustrationPlaceholder(Modifier.fillMaxSize())
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
        )
    }
}
