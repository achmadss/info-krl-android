package dev.achmad.domain.di

import dev.achmad.data.di.dataModule
import dev.achmad.data.repository.ScheduleRepositoryImpl
import dev.achmad.data.repository.StationRepositoryImpl
import dev.achmad.domain.repository.ScheduleRepository
import dev.achmad.domain.repository.StationRepository
import org.koin.dsl.module

val domainModule = module {
    includes(dataModule)

    // repositories
    single<StationRepository> { StationRepositoryImpl(get(), get()) }
    single<ScheduleRepository> { ScheduleRepositoryImpl(get(), get()) }

}