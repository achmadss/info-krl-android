package dev.achmad.infokrl.screens.settings.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.achmad.domain.layout.ScheduleLayouts
import dev.achmad.infokrl.R

@Composable
fun scheduleLayoutOptions(): Map<ScheduleLayouts, String> {
    return ScheduleLayouts.entries.associateWith {
        when(it) {
            ScheduleLayouts.COMPACT -> stringResource(R.string.compact_schedule_layout)
            ScheduleLayouts.COMFORTABLE -> stringResource(R.string.comfortable_schedule_layout)
            ScheduleLayouts.DETAILED -> stringResource(R.string.detailed_schedule_layout)
        }
    }
}