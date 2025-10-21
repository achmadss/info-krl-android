package dev.achmad.infokrl.base

import androidx.annotation.StringRes
import dev.achmad.core.preference.PreferenceStore
import dev.achmad.core.preference.getEnum
import dev.achmad.infokrl.R

class ApplicationPreference(
    private val preferenceStore: PreferenceStore
) {
    fun hasCompletedOnboarding() = preferenceStore.getBoolean(
        key = "has_completed_onboarding",
        defaultValue = false
    )

    fun hasFetchedStations() = preferenceStore.getBoolean(
        key = "has_fetch_stations",
        defaultValue = false
    )

    fun lastFetchSchedule(stationId: String) = preferenceStore.getLong(
        key = "last_fetch_schedule_$stationId",
        defaultValue = 0L
    )

    fun lastFetchRoute(trainId: String) = preferenceStore.getLong(
        key = "last_fetch_route_$trainId",
        defaultValue = 0L
    )

    fun is24HourFormat() = preferenceStore.getBoolean(
        key = "is_24_hour_format",
        defaultValue = true
    )

    fun appTheme() = preferenceStore.getEnum(
        key = "app_theme",
        defaultValue = Themes.SYSTEM,
    )

    enum class Themes(@param:StringRes val stringRes: Int) {
        LIGHT(R.string.theme_light),
        DARK(R.string.theme_dark),
        SYSTEM(R.string.theme_system);
    }
}