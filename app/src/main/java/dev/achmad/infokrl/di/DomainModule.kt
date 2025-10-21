package dev.achmad.infokrl.di

import dev.achmad.data.repository.RouteRepositoryImpl
import dev.achmad.data.repository.ScheduleRepositoryImpl
import dev.achmad.data.repository.StationRepositoryImpl
import dev.achmad.domain.preference.ApplicationPreference
import dev.achmad.domain.route.interactor.GetRoute
import dev.achmad.domain.route.interactor.SyncRoute
import dev.achmad.domain.route.repository.RouteRepository
import dev.achmad.domain.schedule.interactor.GetSchedule
import dev.achmad.domain.schedule.interactor.SyncSchedule
import dev.achmad.domain.schedule.repository.ScheduleRepository
import dev.achmad.domain.station.interactor.GetStation
import dev.achmad.domain.station.interactor.ReorderFavoriteStations
import dev.achmad.domain.station.interactor.SyncStation
import dev.achmad.domain.station.interactor.ToggleFavoriteStation
import dev.achmad.domain.station.repository.StationRepository
import org.koin.dsl.module

val domainModule = module {
    single<StationRepository> { StationRepositoryImpl(get(), get()) }
    factory { GetStation(get()) }
    factory { SyncStation(get()) }
    factory { ToggleFavoriteStation(get()) }
    factory { ReorderFavoriteStations(get()) }

    single<ScheduleRepository> { ScheduleRepositoryImpl(get(), get()) }
    factory { GetSchedule(get()) }
    factory { SyncSchedule(get()) }

    single<RouteRepository> { RouteRepositoryImpl(get(), get()) }
    factory { GetRoute(get()) }
    factory { SyncRoute(get()) }

    single<ApplicationPreference> { ApplicationPreference(get()) }
}