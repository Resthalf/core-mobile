package com.resthalflab.resthalfapp.feature.search.ui

import androidx.compose.runtime.Composable
import com.resthalflab.resthalfapp.feature.search.ui.home.HomeScreen

@Composable
fun SearchTab(component: SearchTabComponent) {
    HomeScreen(component.home)
}
