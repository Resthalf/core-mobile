package com.resthalflab.resthalfapp.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resthalflab.resthalfapp.core.design.components.RhOutlinedButton
import com.resthalflab.resthalfapp.core.design.components.RhScaffold

@Composable
fun ProfileScreen(component: ProfileComponent) {
    val session by component.session.collectAsStateWithLifecycle()

    RhScaffold(title = "Profile") { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = session?.email ?: "Signed in",
                style = MaterialTheme.typography.titleMedium,
            )
            RhOutlinedButton(text = "Sign out", onClick = component::onLogoutClicked)
        }
    }
}
