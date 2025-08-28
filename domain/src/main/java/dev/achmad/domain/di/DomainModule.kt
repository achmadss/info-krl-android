package dev.achmad.domain.di

import dev.achmad.data.di.dataModule
import org.koin.dsl.module

val domainModule = module {
    includes(dataModule)
}