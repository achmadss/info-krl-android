package dev.achmad.core.di

import android.content.Context
import dev.achmad.core.BuildConfig
import dev.achmad.core.network.NetworkHelper
import dev.achmad.core.preference.AndroidPreferenceStore
import dev.achmad.core.preference.PreferenceStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { NetworkHelper(androidContext(), BuildConfig.DEBUG) }
    single<PreferenceStore> {
        AndroidPreferenceStore(
            androidContext().getSharedPreferences("app_pref", Context.MODE_PRIVATE)
        )
    }
}