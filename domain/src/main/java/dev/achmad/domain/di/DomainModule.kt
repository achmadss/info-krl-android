package dev.achmad.domain.di

import dev.achmad.domain.usecase.route.GetRoute
import dev.achmad.domain.usecase.route.SyncRoute
import dev.achmad.domain.usecase.station.SyncStation
import dev.achmad.domain.usecase.schedule.GetSchedule
import dev.achmad.domain.usecase.station.GetStation
import dev.achmad.domain.usecase.station.ReorderFavoriteStations
import dev.achmad.domain.usecase.schedule.SyncSchedule
import dev.achmad.domain.usecase.station.ToggleFavoriteStation
import org.koin.dsl.module

val domainModule = module {
    single { GetSchedule(get()) }
    single { GetStation(get()) }
    single { GetRoute(get()) }
    single { SyncSchedule(get()) }
    single { SyncStation(get()) }
    single { SyncRoute(get()) }
    single { ToggleFavoriteStation(get()) }
    factory { ReorderFavoriteStations(get()) }
}
