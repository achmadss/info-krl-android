package dev.achmad.infokrl.di

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import dev.achmad.core.BuildConfig
import dev.achmad.core.network.NetworkHelper
import dev.achmad.core.preference.AndroidPreferenceStore
import dev.achmad.core.preference.PreferenceStore
import dev.achmad.core.util.ToastHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { ToastHelper(androidContext()) }
    single { NetworkHelper(androidContext(), BuildConfig.DEBUG) }
    single<PreferenceStore> {
        AndroidPreferenceStore(
            androidContext().getSharedPreferences("app_pref", Context.MODE_PRIVATE)
        )
    }
    single<AppUpdateManager> { AppUpdateManagerFactory.create(androidContext()) }
}