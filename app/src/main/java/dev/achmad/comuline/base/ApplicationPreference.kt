package dev.achmad.comuline.base

import dev.achmad.core.preference.PreferenceStore

class ApplicationPreference(
    private val preferenceStore: PreferenceStore
) {
    fun isFirstRun() = preferenceStore.getBoolean("first_run", true)
    fun lastFetchSchedule(stationId: String) = preferenceStore.getLong("last_fetch_schedule_$stationId")
}