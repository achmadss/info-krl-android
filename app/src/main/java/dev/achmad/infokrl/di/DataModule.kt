package dev.achmad.infokrl.di

import androidx.room.Room
import dev.achmad.data.local.InfoKRLDatabase
import dev.achmad.data.remote.InfoKRLApi
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single<InfoKRLApi> { InfoKRLApi(get(), get()) }
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
}