package dev.achmad.infokrl.screens.settings.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.achmad.domain.layout.ScheduleLayouts
import dev.achmad.infokrl.R

@Composable
fun scheduleLayoutOptions(): Map<ScheduleLayouts, String> {
    return ScheduleLayouts.entries.associateWith {
        when(it) {
            ScheduleLayouts.MINIMAL -> stringResource(R.string.minimal_schedule_layout)
            ScheduleLayouts.NORMAL -> stringResource(R.string.normal_schedule_layout)
        }
    }
}