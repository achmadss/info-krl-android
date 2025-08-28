package dev.achmad.data.di

import androidx.room.Room
import dev.achmad.data.local.ComulineDatabase
import dev.achmad.data.local.dao.ScheduleDao
import dev.achmad.data.local.dao.StationDao
import dev.achmad.data.remote.ComulineApi
import dev.achmad.data.remote.ComulineApiPreference
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

    // dao
    single<StationDao> { get<ComulineDatabase>().stationDao() }
    single<ScheduleDao> { get<ComulineDatabase>().scheduleDao() }

}