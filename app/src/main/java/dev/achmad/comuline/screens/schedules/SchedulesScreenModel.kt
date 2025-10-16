package dev.achmad.comuline.screens.schedules

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.comuline.util.etaString
import dev.achmad.comuline.work.SyncRouteJob
import dev.achmad.comuline.work.SyncScheduleJob
import dev.achmad.core.di.util.inject
import dev.achmad.core.di.util.injectContext
import dev.achmad.core.util.TimeTicker
import dev.achmad.domain.model.Route
import dev.achmad.domain.model.Schedule
import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.RouteRepository
import dev.achmad.domain.repository.ScheduleRepository
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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
    val schedules: List<UISchedule>
) {
    data class UISchedule(
        val schedule: Schedule,
        val eta: String,
        val stops: Int?,
    )
}

class SchedulesScreenModel(
    private val originStationId: String,
    private val destinationStationId: String,
    private val scheduleRepository: ScheduleRepository = inject(),
    private val stationRepository: StationRepository = inject(),
    private val routeRepository: RouteRepository = inject()
): ScreenModel {

    private val scheduleFlowsCache = mutableMapOf<String, StateFlow<List<Schedule>?>>()
    private val routeFlowsCache = mutableMapOf<String, StateFlow<Route?>>()
    private val _routeUpdateTrigger = MutableStateFlow(0L)

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
        _routeUpdateTrigger
    ) { _, schedules, originStation, destinationStation, _ ->
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
                        val routeFlow = getRouteFlow(schedule.trainId)
                        val stopsCount = calculateStopsCount(
                            route = routeFlow.value,
                            originStationId = originStationId,
                        )
                        ScheduleGroup.UISchedule(
                            schedule = schedule,
                            eta = etaString(
                                now = LocalDateTime.now(),
                                target = schedule.departsAt,
                                compactMode = false
                            ),
                            stops = stopsCount
                        )
                    }
                    .also {
                        if (it.isNotEmpty()) {
                            fetchRoute(it.map { it.schedule.trainId })
                        }
                    }
                ScheduleGroup(
                    originStation = originStation,
                    destinationStation = destinationStation,
                    schedules = filteredSchedules
                )
            }
        }
    }.distinctUntilChanged().stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private fun calculateStopsCount(
        route: Route?,
        originStationId: String,
    ): Int? {
        if (route == null) return null

        val stopStationIds = route.stops.map { it.stationId }
        val bstStationsIds = listOf("SUDB", "DU", "RW", "BPR")

        return stopStationIds
            .indexOf(originStationId)
            .takeIf { it != -1 }
            ?.let { index -> stopStationIds.drop(index + 1) }
            ?.let { remainingStops ->
                if (route.line.contains("BST")) {
                    remainingStops.filter { stationId -> stationId in bstStationsIds }
                } else remainingStops
            }
            ?.size
    }

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
        return routeFlowsCache.getOrPut(trainId) {
            routeRepository.subscribeSingle(trainId)
                .stateIn(
                    scope = screenModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = null
                ).also { flow ->
                    screenModelScope.launch {
                        flow.collect { route ->
                            if (route != null) {
                                _routeUpdateTrigger.value = System.currentTimeMillis()
                            }
                        }
                    }
                }
        }
    }

    private fun fetchRoute(trainIds: List<String>) {
        screenModelScope.launch(Dispatchers.IO) {
            SyncRouteJob.start(
                context = injectContext(),
                trainIds = trainIds,
                finishDelay = 500
            )
        }
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