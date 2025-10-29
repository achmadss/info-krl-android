package dev.achmad.infokrl.screens.schedules

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.util.inject
import dev.achmad.core.util.injectContext
import dev.achmad.core.util.TimeTicker
import dev.achmad.domain.schedule.interactor.GetSchedule
import dev.achmad.domain.schedule.interactor.SyncSchedule
import dev.achmad.domain.schedule.model.Schedule
import dev.achmad.domain.station.interactor.GetStation
import dev.achmad.domain.station.model.Station
import dev.achmad.infokrl.util.etaString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class ScheduleGroup(
    val originStation: Station,
    val destinationStation: Station,
    val currentTime: LocalDateTime,
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
    private val line: String,
    private val getSchedule: GetSchedule = inject(),
    private val syncSchedule: SyncSchedule = inject(),
    private val getStation: GetStation = inject(),
): ScreenModel {

    var backFromTimeline by mutableStateOf(false)

    private val scheduleFlowsCache = mutableMapOf<String, StateFlow<List<Schedule>?>>()

    private val _syncScheduleResult = MutableStateFlow<SyncSchedule.Result?>(null)
    val syncScheduleResult = _syncScheduleResult.asStateFlow()

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
    ) { tickValue, schedules, originStation, destinationStation ->
        when {
            schedules == null -> null
            originStation == null -> null
            destinationStation == null -> null
            else -> {
                val filteredSchedules = schedules
                    .filter { it.stationDestinationId == destinationStationId }
                    .filter { it.line == line }
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
                    currentTime = tickValue ?: LocalDateTime.now(),
                    schedules = filteredSchedules,
                )
            }
        }
    }.distinctUntilChanged().stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
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
        return getStation.subscribeSingle(stationId).stateIn(
            scope = screenModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )
    }

    fun fetchSchedule() {
        screenModelScope.launch {
            syncSchedule.subscribe(originStationId).collect {
                _syncScheduleResult.value = it
            }
        }
    }

}
