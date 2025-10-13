package dev.achmad.comuline.base

import dev.achmad.core.preference.PreferenceStore

class ApplicationPreference(
    private val preferenceStore: PreferenceStore
) {
    fun hasFetchedStations() = preferenceStore.getBoolean("first_run", false)
    fun lastFetchSchedule(stationId: String) = preferenceStore.getLong("last_fetch_schedule_$stationId")
    fun lastFetchRoute(trainId: String) = preferenceStore.getLong("last_fetch_route_$trainId")
    fun timeFormat() = preferenceStore.getBoolean("use_24_hour_format", true)
}