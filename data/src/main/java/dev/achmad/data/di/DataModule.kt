package dev.achmad.data.di

import androidx.room.Room
import dev.achmad.data.local.ComulineDatabase
import dev.achmad.data.remote.ComulineApi
import dev.achmad.data.remote.ComulineApiPreference
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
    single<ComulineApi> { ComulineApi(get(), get()) }
    single<ComulineApiPreference> { ComulineApiPreference(get()) }

    // database
    single<ComulineDatabase> {
        Room
            .databaseBuilder(
                androidContext(),
                ComulineDatabase::class.java,
                "comuline_db"
            )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    // repositories
    single<StationRepository> { StationRepositoryImpl(get(), get()) }
    single<ScheduleRepository> { ScheduleRepositoryImpl(get(), get()) }
    single<RouteRepository> { RouteRepositoryImpl(get(), get()) }

}