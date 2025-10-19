package dev.achmad.infokrl.screens.schedules

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.infokrl.util.calculateStopsCount
import dev.achmad.core.di.util.inject
import dev.achmad.core.di.util.injectContext
import dev.achmad.core.util.TimeTicker
import dev.achmad.domain.model.Route
import dev.achmad.domain.model.Schedule
import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.RouteRepository
import dev.achmad.domain.repository.ScheduleRepository
import dev.achmad.domain.repository.StationRepository
import dev.achmad.infokrl.util.etaString
import dev.achmad.infokrl.work.SyncRouteJob
import dev.achmad.infokrl.work.SyncScheduleJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalDateTime

data class ScheduleGroup(
    val originStation: Station,
    val destinationStation: Station,
    val schedules: List<UISchedule>,
    val maxStops: Int?
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

    // Track which train IDs have been requested to avoid duplicate fetches
    // Use Mutex to prevent race conditions when multiple scroll events occur
    private val requestedTrainIds = mutableSetOf<String>()
    private val requestedTrainIdsMutex = Mutex()

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
        _routeUpdateTrigger,
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
                                context = injectContext(),
                                now = LocalDateTime.now(),
                                target = schedule.departsAt,
                                compactMode = false
                            ),
                            stops = stopsCount
                        )
                    }
                val maxStops = filteredSchedules.mapNotNull { it.stops }.maxOrNull()

                ScheduleGroup(
                    originStation = originStation,
                    destinationStation = destinationStation,
                    schedules = filteredSchedules,
                    maxStops = maxStops
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
                    // Monitor this flow and trigger updates when route loads
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

    /**
     * Fetches routes for specific schedules. Only fetches if not already requested.
     * Called by the UI layer when schedules become visible.
     * Thread-safe with mutex to prevent race conditions from rapid scroll events.
     */
    fun fetchRoutesForSchedules(scheduleIds: List<String>) {
        screenModelScope.launch(Dispatchers.IO) {
            val schedules = scheduleGroup.value?.schedules ?: return@launch

            // Get train IDs for the requested schedule IDs
            val candidateTrainIds = schedules
                .filter { it.schedule.id in scheduleIds }
                .map { it.schedule.trainId }
                .distinct()

            // Thread-safe filtering and marking
            val trainIdsToFetch = requestedTrainIdsMutex.withLock {
                val newTrainIds = candidateTrainIds.filter { it !in requestedTrainIds }
                // Mark as requested before releasing lock to prevent race conditions
                requestedTrainIds.addAll(newTrainIds)
                newTrainIds
            }

            if (trainIdsToFetch.isNotEmpty()) {
                SyncRouteJob.start(
                    context = injectContext(),
                    trainIds = trainIdsToFetch,
                    finishDelay = 500
                )
            }
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