package dev.achmad.infokrl.screens.schedules

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.core.di.util.injectContext
import dev.achmad.core.util.TimeTicker
import dev.achmad.domain.model.Schedule
import dev.achmad.domain.model.Station
import dev.achmad.domain.usecase.schedule.GetSchedule
import dev.achmad.domain.usecase.station.GetStation
import dev.achmad.infokrl.util.etaString
import dev.achmad.infokrl.work.SyncScheduleJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class ScheduleGroup(
    val originStation: Station,
    val destinationStation: Station,
    val schedules: List<UISchedule>,
) {
    data class UISchedule(
        val schedule: Schedule,
        val eta: String,
    )
}

class SchedulesScreenModel(
    private val originStationId: String,
    private val destinationStationId: String,
    private val getSchedule: GetSchedule = inject(),
    private val getStation: GetStation = inject(),
): ScreenModel {

    private val scheduleFlowsCache = mutableMapOf<String, StateFlow<List<Schedule>?>>()

    private val tick = TimeTicker(TimeTicker.TickUnit.MINUTE).ticks.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    init {
        fetchSchedule()
    }

    val scheduleGroup: StateFlow<ScheduleGroup?> = combine(
        tick,
        getScheduleFlow(originStationId),
        getStationFlow(originStationId),
        getStationFlow(destinationStationId),
    ) { _, schedules, originStation, destinationStation ->
        when {
            schedules == null -> null
            originStation == null -> null
            destinationStation == null -> null
            else -> {
                val filteredSchedules = schedules
                    .filter { it.stationDestinationId == destinationStationId }
                    .filter { it.departsAt.toLocalDate() == LocalDate.now() }
                    .sortedBy { it.departsAt }
                    .map { schedule ->
                        ScheduleGroup.UISchedule(
                            schedule = schedule,
                            eta = etaString(
                                context = injectContext(),
                                now = LocalDateTime.now(),
                                target = schedule.departsAt,
                                compactMode = false
                            ),
                        )
                    }

                ScheduleGroup(
                    originStation = originStation,
                    destinationStation = destinationStation,
                    schedules = filteredSchedules,
                )
            }
        }
    }.distinctUntilChanged().stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private fun getScheduleFlow(stationId: String): StateFlow<List<Schedule>?> {
        return scheduleFlowsCache.getOrPut(stationId) {
            getSchedule.subscribe(stationId).stateIn(
                scope = screenModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )
        }
    }

    private fun getStationFlow(stationId: String): StateFlow<Station?> {
        return getStation.subscribe(stationId).stateIn(
            scope = screenModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )
    }

    private fun fetchSchedule() {
        screenModelScope.launch(Dispatchers.IO) {
            SyncScheduleJob.start(
                context = injectContext(),
                stationId = originStationId,
                finishDelay = 1000L
            )
        }
    }

}