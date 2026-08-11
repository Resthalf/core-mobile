package com.resthalflab.resthalfapp.feature.bookings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingTime
import kotlinx.coroutines.delay

/** Ticks every second, returning a compact "Xh Ym left" label for an active booking. */
@Composable
fun rememberRemainingLabel(endIso: String): String {
    var label by remember(endIso) { mutableStateOf(BookingTime.compactRemaining(endIso)) }
    LaunchedEffect(endIso) {
        while (true) {
            label = BookingTime.compactRemaining(endIso)
            delay(1_000)
        }
    }
    return label
}
