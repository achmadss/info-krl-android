package dev.achmad.infokrl.di

import dev.achmad.infokrl.base.ApplicationPreference
import org.koin.dsl.module

val appModule = module {
    single<ApplicationPreference> { ApplicationPreference(get()) }
}