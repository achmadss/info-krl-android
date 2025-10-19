package dev.achmad.data.remote

import dev.achmad.core.preference.PreferenceStore

class InfoKRLApiPreference(
    private val preferenceStore: PreferenceStore
) {
    fun baseUrl() = preferenceStore.getString(
        key = "api_base_url",
        defaultValue = "https://comuline-api.achmad.dev"
    )
}