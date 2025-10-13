package dev.achmad.comuline.screens.home.station_detail

import android.util.Log
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.comuline.util.etaString
import dev.achmad.comuline.work.SyncRouteJob
import dev.achmad.core.di.util.inject
import dev.achmad.core.di.util.injectContext
import dev.achmad.core.util.TimeTicker
import dev.achmad.domain.model.Route
import dev.achmad.domain.model.Schedule
import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.RouteRepository
import dev.achmad.domain.repository.ScheduleRepository
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class ScheduleGroup(
    val originStation: Station,
    val destinationStation: Station,
    val schedules: List<UISchedule>
) {
    data class UISchedule(
        val schedule: Schedule,
        val route: StateFlow<Route?>,
        val eta: String,
    )
}

class StationDetailScreenModel(
    private val originStationId: String,
    private val destinationStationId: String,
    private val scheduleRepository: ScheduleRepository = inject(),
    private val stationRepository: StationRepository = inject(),
    private val routeRepository: RouteRepository = inject()
): ScreenModel {

    private val scheduleFlowsCache = mutableMapOf<String, StateFlow<List<Schedule>?>>()

    private val tick = TimeTicker(TimeTicker.TickUnit.MINUTE).ticks.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val schedules: StateFlow<ScheduleGroup?> = combine(
        tick,
        getScheduleFlow(originStationId),
        getStationFlow(originStationId),
        getStationFlow(destinationStationId)
    ) { _, schedules, originStation, destinationStation ->
        when {
            schedules == null -> null
            originStation == null -> null
            destinationStation == null -> null
            else -> {
                val filteredSchedules = schedules
                    .filter { it.stationDestinationId == destinationStationId }
                    .filter {
                        it.departsAt.toLocalDate() ==
                                LocalDate.now()
                    }
                    .sortedBy { it.departsAt }
                    .let {
                        screenModelScope.launch {
                            fetchRoute(it.map { it.trainId })
                        }
                        it
                    }
                    .map { schedule ->
                        ScheduleGroup.UISchedule(
                            schedule = schedule,
                            eta = etaString(
                                now = LocalDateTime.now(),
                                target = schedule.departsAt,
                                compactMode = false
                            ),
                            route = getRouteFlow(schedule.trainId),
                        )
                    }

                ScheduleGroup(
                    originStation = originStation,
                    destinationStation = destinationStation,
                    schedules = filteredSchedules
                )
            }
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private fun getScheduleFlow(stationId: String): StateFlow<List<Schedule>?> {
        return scheduleFlowsCache.getOrPut(stationId) {
            scheduleRepository.subscribeSingle(
                stationId = stationId,
            ).stateIn(
                scope = screenModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )
        }
    }

    private fun getStationFlow(stationId: String): StateFlow<Station?> {
        return stationRepository.subscribeSingle(stationId)
            .stateIn(
                scope = screenModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )
    }

    private fun getRouteFlow(trainId: String): StateFlow<Route?> {
        return routeRepository.subscribeSingle(trainId)
            .stateIn(
            scope = screenModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )
    }

    private fun fetchRoute(trainIds: List<String>) {
        SyncRouteJob.start(
            context = injectContext(),
            trainIds = trainIds,
            finishDelay = 500
        )
    }

}