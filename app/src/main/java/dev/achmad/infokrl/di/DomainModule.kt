package dev.achmad.infokrl.di

import dev.achmad.data.repository.RouteRepositoryImpl
import dev.achmad.data.repository.ScheduleRepositoryImpl
import dev.achmad.data.repository.StationRepositoryImpl
import dev.achmad.domain.preference.ApplicationPreference
import dev.achmad.domain.route.interactor.GetRoute
import dev.achmad.domain.route.interactor.ShouldSyncRoute
import dev.achmad.domain.route.interactor.SyncRoute
import dev.achmad.domain.route.interactor.WipeRouteTables
import dev.achmad.domain.route.repository.RouteRepository
import dev.achmad.domain.schedule.interactor.GetSchedule
import dev.achmad.domain.schedule.interactor.ShouldSyncSchedule
import dev.achmad.domain.schedule.interactor.SyncSchedule
import dev.achmad.domain.schedule.interactor.WipeScheduleTables
import dev.achmad.domain.schedule.repository.ScheduleRepository
import dev.achmad.domain.station.interactor.GetStation
import dev.achmad.domain.station.interactor.HasFetchedStations
import dev.achmad.domain.station.interactor.ReorderFavoriteStations
import dev.achmad.domain.station.interactor.SyncStation
import dev.achmad.domain.station.interactor.ToggleFavoriteStation
import dev.achmad.domain.station.interactor.WipeStationTables
import dev.achmad.domain.station.repository.StationRepository
import org.koin.dsl.module

val domainModule = module {
    single<StationRepository> { StationRepositoryImpl(get(), get()) }
    factory { GetStation(get()) }
    factory { HasFetchedStations(get()) }
    factory { ReorderFavoriteStations(get()) }
    factory { SyncStation(get()) }
    factory { ToggleFavoriteStation(get()) }
    factory { WipeStationTables(get()) }

    single<ScheduleRepository> { ScheduleRepositoryImpl(get(), get()) }
    factory { GetSchedule(get()) }
    factory { ShouldSyncSchedule(get()) }
    factory { SyncSchedule(get()) }
    factory { WipeScheduleTables(get()) }

    single<RouteRepository> { RouteRepositoryImpl(get(), get()) }
    factory { GetRoute(get()) }
    factory { ShouldSyncRoute(get()) }
    factory { SyncRoute(get()) }
    factory { WipeRouteTables(get()) }

    single<ApplicationPreference> { ApplicationPreference(get()) }
}