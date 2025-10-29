package dev.achmad.domain.preference

import dev.achmad.core.preference.PreferenceStore
import dev.achmad.core.preference.getEnum
import dev.achmad.domain.layout.ScheduleLayouts
import dev.achmad.domain.theme.Themes

class ApplicationPreference(
    private val preferenceStore: PreferenceStore
) {
    fun scheduleLayoutType() = preferenceStore.getEnum(
        key = "schedule_layout_type",
        defaultValue = ScheduleLayouts.COMFORTABLE
    )

    fun hasCompletedOnboarding() = preferenceStore.getBoolean(
        key = "has_completed_onboarding",
        defaultValue = false
    )

    fun baseUrl() = preferenceStore.getString(
        key = "api_base_url",
        defaultValue = "https://info-krl-api.achmad.dev"
    )

    fun is24HourFormat() = preferenceStore.getBoolean(
        key = "is_24_hour_format",
        defaultValue = true
    )

    fun appTheme() = preferenceStore.getEnum(
        key = "app_theme",
        defaultValue = Themes.SYSTEM,
    )
}