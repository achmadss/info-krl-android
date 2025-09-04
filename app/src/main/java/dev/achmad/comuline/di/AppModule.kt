package dev.achmad.comuline.di

import dev.achmad.comuline.base.ApplicationPreference
import org.koin.dsl.module

val appModule = module {
    single<ApplicationPreference> { ApplicationPreference(get()) }
}