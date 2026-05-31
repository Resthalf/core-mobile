package com.resthalflab.resthalfapp.core.design.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

/**
 * Loads a remote image via Kamel, falling back to [RhIllustrationPlaceholder] while loading or on
 * failure. Single place to manage image loading — swap the loader here if needed.
 */
@Composable
fun RhRemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    KamelImage(
        resource = asyncPainterResource(data = url),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        onLoading = { RhIllustrationPlaceholder(Modifier.matchParentSize()) },
        onFailure = { RhIllustrationPlaceholder(Modifier.matchParentSize()) },
    )
}
