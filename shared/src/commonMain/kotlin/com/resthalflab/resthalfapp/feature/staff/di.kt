package com.resthalflab.resthalfapp.feature.staff

import com.arkivanov.decompose.ComponentContext
import com.resthalflab.resthalfapp.feature.search.api.SearchArgs
import com.resthalflab.resthalfapp.feature.staff.data.DashboardRemote
import com.resthalflab.resthalfapp.feature.staff.data.DefaultDashboardRepository
import com.resthalflab.resthalfapp.feature.staff.domain.ConfirmVacateUseCase
import com.resthalflab.resthalfapp.feature.staff.domain.DashboardRepository
import com.resthalflab.resthalfapp.feature.staff.domain.GetCheckInsUseCase
import com.resthalflab.resthalfapp.feature.staff.domain.GetRoomsUseCase
import com.resthalflab.resthalfapp.feature.staff.ui.checkins.CheckInsComponent
import com.resthalflab.resthalfapp.feature.staff.ui.checkins.DefaultCheckInsComponent
import com.resthalflab.resthalfapp.feature.staff.ui.home.DefaultStaffHomeComponent
import com.resthalflab.resthalfapp.feature.staff.ui.home.StaffHomeComponent
import com.resthalflab.resthalfapp.feature.staff.ui.rooms.DefaultRoomsComponent
import com.resthalflab.resthalfapp.feature.staff.ui.rooms.RoomsComponent
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module

val staffModule: Module = module {
    single { DashboardRemote(get()) }
    single<DashboardRepository> { DefaultDashboardRepository(get()) }
    factory { GetCheckInsUseCase(get()) }
    factory { GetRoomsUseCase(get()) }
    factory { ConfirmVacateUseCase(get()) }
}

fun staffHomeComponent(
    componentContext: ComponentContext,
    koin: Koin,
    onOpenSearchResults: (SearchArgs) -> Unit,
    onOpenRooms: (String) -> Unit,
): StaffHomeComponent = DefaultStaffHomeComponent(
    componentContext = componentContext,
    locationSearch = koin.get(),
    onSearch = onOpenSearchResults,
    getRooms = koin.get(),
    confirmVacate = koin.get(),
    onOpenRooms = onOpenRooms,
)

fun checkInsComponent(
    componentContext: ComponentContext,
    koin: Koin,
): CheckInsComponent = DefaultCheckInsComponent(
    componentContext = componentContext,
    getCheckIns = koin.get(),
)

fun roomsComponent(
    componentContext: ComponentContext,
    koin: Koin,
): RoomsComponent = DefaultRoomsComponent(
    componentContext = componentContext,
    getRooms = koin.get(),
)
