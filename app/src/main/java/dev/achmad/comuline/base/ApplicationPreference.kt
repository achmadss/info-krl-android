package dev.achmad.comuline.base

import dev.achmad.core.preference.PreferenceStore

class ApplicationPreference(
    private val preferenceStore: PreferenceStore
) {
    fun hasFetchedStations() = preferenceStore.getBoolean(
        key = "first_run",
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

    fun timeFormat() = preferenceStore.getBoolean(
        key = "time_format",
        defaultValue = true
    )

}