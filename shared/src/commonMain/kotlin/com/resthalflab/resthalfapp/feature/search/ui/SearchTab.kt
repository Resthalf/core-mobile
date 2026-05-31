package com.resthalflab.resthalfapp.feature.search.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.resthalflab.resthalfapp.feature.search.ui.home.HomeScreen
import com.resthalflab.resthalfapp.feature.search.ui.results.ResultsScreen

@Composable
fun SearchTab(component: SearchTabComponent) {
    Children(stack = component.stack) { child ->
        when (val instance = child.instance) {
            is SearchTabComponent.Child.Home -> HomeScreen(instance.component)
            is SearchTabComponent.Child.Results -> ResultsScreen(instance.component)
        }
    }
}
