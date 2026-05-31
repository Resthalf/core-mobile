package com.resthalflab.resthalfapp.feature.bookings

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.bookings.ui.BookingsComponent
import com.resthalflab.resthalfapp.feature.bookings.ui.DefaultBookingsComponent

/** Feature entry point. Placeholder until bookings data lands. */
fun bookingsComponent(componentContext: ComponentContext): BookingsComponent =
    DefaultBookingsComponent(componentContext)
