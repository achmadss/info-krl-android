package dev.achmad.data.di

import androidx.room.Room
import dev.achmad.data.local.InfoKRLDatabase
import dev.achmad.data.remote.InfoKRLApi
import dev.achmad.data.remote.InfoKRLApiPreference
import dev.achmad.data.repository.RouteRepositoryImpl
import dev.achmad.data.repository.ScheduleRepositoryImpl
import dev.achmad.data.repository.StationRepositoryImpl
import dev.achmad.domain.repository.RouteRepository
import dev.achmad.domain.repository.ScheduleRepository
import dev.achmad.domain.repository.StationRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    // api
    single<InfoKRLApi> { InfoKRLApi(get(), get()) }
    single<InfoKRLApiPreference> { InfoKRLApiPreference(get()) }

    // database
    single<InfoKRLDatabase> {
        Room
            .databaseBuilder(
                androidContext(),
                InfoKRLDatabase::class.java,
                "infokrl_db"
            )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    // repositories
    single<StationRepository> { StationRepositoryImpl(get(), get()) }
    single<ScheduleRepository> { ScheduleRepositoryImpl(get(), get()) }
    single<RouteRepository> { RouteRepositoryImpl(get(), get()) }

}