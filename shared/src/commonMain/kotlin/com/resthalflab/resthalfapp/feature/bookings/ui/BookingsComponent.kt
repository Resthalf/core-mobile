package com.resthalflab.resthalfapp.feature.bookings.ui

import com.arkivanov.decompose.ComponentContext

interface BookingsComponent

class DefaultBookingsComponent(
    componentContext: ComponentContext,
) : BookingsComponent, ComponentContext by componentContext
