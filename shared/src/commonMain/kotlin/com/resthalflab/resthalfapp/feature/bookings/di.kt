package com.resthalflab.resthalfapp.feature.bookings

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.bookings.data.BookingsRemote
import com.resthalflab.resthalfapp.feature.bookings.data.DefaultBookingsRepository
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingsRepository
import com.resthalflab.resthalfapp.feature.bookings.domain.GetBookingByIdUseCase
import com.resthalflab.resthalfapp.feature.bookings.domain.GetBookingsUseCase
import com.resthalflab.resthalfapp.feature.bookings.domain.VacateBookingUseCase
import com.resthalflab.resthalfapp.feature.bookings.ui.BookingsComponent
import com.resthalflab.resthalfapp.feature.bookings.ui.DefaultBookingsComponent
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module

val bookingsModule: Module = module {
    single { BookingsRemote(get()) }
    single<BookingsRepository> { DefaultBookingsRepository(get(), get()) }
    factory { GetBookingsUseCase(get()) }
    factory { GetBookingByIdUseCase(get()) }
    factory { VacateBookingUseCase(get()) }
}

/** BookingDetail is a root destination — only the list tab is built here. */
fun bookingsComponent(
    componentContext: ComponentContext,
    koin: Koin,
    onOpenBookingDetail: (String) -> Unit,
): BookingsComponent = DefaultBookingsComponent(
    componentContext = componentContext,
    getBookings = koin.get(),
    onOpenBookingDetail = onOpenBookingDetail,
)
