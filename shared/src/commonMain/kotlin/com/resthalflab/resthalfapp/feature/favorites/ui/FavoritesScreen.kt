package com.resthalflab.resthalfapp.feature.favorites.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.resthalflab.resthalfapp.core.design.RhSpacing
import com.resthalflab.resthalfapp.core.design.components.RhScaffold

@Composable
fun FavoritesScreen(@Suppress("UNUSED_PARAMETER") component: FavoritesComponent) {
    RhScaffold(title = "Favorites") { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(RhSpacing.sm),
            ) {
                Text("No favorites yet", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Tap the heart on a stay to save it here.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
