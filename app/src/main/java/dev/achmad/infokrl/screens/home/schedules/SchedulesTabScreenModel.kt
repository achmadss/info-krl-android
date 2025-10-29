package dev.achmad.infokrl.screens.home.schedules

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

private const val sharingStartedStopTimeout = 5_000L

data class DepartureGroup(
    val station: Station,
    val scheduleGroup: StateFlow<List<ScheduleGroup>?>
) {
    data class ScheduleGroup(
        val line: String,
        val color: String?,
        val destinationGroups: List<DestinationGroup>
    ) {
        data class DestinationGroup(
            val destinationStation: Station,
            val schedules: List<UISchedule>
        ) {
            data class UISchedule(
                val schedule: Schedule,
                val eta: String,
            )
        }
    }
}

class SchedulesTabScreenModel(
    private val getSchedule: GetSchedule = inject(),
    private val syncSchedule: SyncSchedule = inject(),
    private val getStation: GetStation = inject(),
): ScreenModel {

    private val scheduleFlowsCache = mutableMapOf<String, StateFlow<List<Schedule>?>>()

    private val _focusedStationId = MutableStateFlow<String?>(null)
    val focusedStationId = _focusedStationId.asStateFlow()

    private val _syncScheduleResult = MutableStateFlow<SyncSchedule.Result?>(null)
    val syncScheduleResult = _syncScheduleResult.asStateFlow()

    private val tick = TimeTicker(TimeTicker.TickUnit.MINUTE).ticks
        .distinctUntilChanged()
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(sharingStartedStopTimeout),
            initialValue = LocalDateTime.now()
        )

    private val favoriteStations = getStation.subscribeAll(favorite = true)
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(sharingStartedStopTimeout),
            initialValue = emptyList()
        )

    val departureGroups = favoriteStations.map { favorites ->
        favorites
            .sortedBy { it.favoritePosition }
            .map { favorite ->
                DepartureGroup(
                    station = favorite,
                    scheduleGroup = createScheduleGroupFlow(favorite.id)
                )
            }
    }.distinctUntilChanged().stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(sharingStartedStopTimeout),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createScheduleGroupFlow(
        stationId: String
    ): StateFlow<List<DepartureGroup.ScheduleGroup>?> {
        val scheduleFlow = getScheduleFlow(stationId)
        return scheduleFlow
            .flatMapLatest { schedules ->
                if (schedules.isNullOrEmpty()) {
                    return@flatMapLatest flowOf(null)
                }
                val destinationIds = schedules.map { it.stationDestinationId }.distinct()
                val destinationStationsFlow = getStation.subscribeMultiple(destinationIds)
                combine(
                    flowOf(schedules),
                    destinationStationsFlow,
                    tick,
                ) { currentSchedules, destinationStations, time ->
                    withContext(Dispatchers.Default) {
                        computeScheduleGroups(
                            schedules = currentSchedules,
                            stations = destinationStations,
                            currentTime = time ?: LocalDateTime.now(),
                        )
                    }
                }
            }
            .distinctUntilChanged()
            .stateIn(
                scope = screenModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )
    }

    /**
     * Computes schedule groups for a station.
     * Extracted to a separate function for better performance and testability.
     */
    private fun computeScheduleGroups(
        schedules: List<Schedule>,
        stations: List<Station>,
        currentTime: LocalDateTime,
    ): List<DepartureGroup.ScheduleGroup> {
        // First group by line
        val schedulesByLine = schedules.groupBy { it.line }
        return schedulesByLine.mapNotNull { (line, schedulesForLine) ->
            // Within each line, group by destination station
            val schedulesByDestination = schedulesForLine.groupBy { it.stationDestinationId }
            
            val destinationGroups = schedulesByDestination.mapNotNull { (destinationId, schedulesForDest) ->
                val destinationStation = stations.firstOrNull { it.id == destinationId }
                destinationStation?.let {
                    val sortedSchedules = schedulesForDest
                        .filter { it.departsAt.isAfter(currentTime) }
                        .sortedBy { it.departsAt }
                    
                    val uiSchedules = sortedSchedules.map { schedule ->
                        DepartureGroup.ScheduleGroup.DestinationGroup.UISchedule(
                            schedule = schedule,
                            eta = etaString(
                                context = injectContext(),
                                now = currentTime,
                                target = schedule.departsAt,
                            ),
                        )
                    }
                    
                    if (uiSchedules.isNotEmpty()) {
                        DepartureGroup.ScheduleGroup.DestinationGroup(
                            destinationStation = destinationStation,
                            schedules = uiSchedules
                        )
                    } else null
                }
            }
            
            if (destinationGroups.isNotEmpty()) {
                DepartureGroup.ScheduleGroup(
                    line = line,
                    color = destinationGroups.firstOrNull()?.schedules?.firstOrNull()?.schedule?.color,
                    destinationGroups = destinationGroups.sortedBy { it.destinationStation.name }
                )
            } else null
        }.sortedBy { it.line }
    }

    private fun getScheduleFlow(stationId: String): StateFlow<List<Schedule>?> {
        val cache = scheduleFlowsCache[stationId]
        if (cache == null) {
            val scheduleFlow = getSchedule.subscribe(stationId).stateIn(
                scope = screenModelScope,
                started = SharingStarted.WhileSubscribed(sharingStartedStopTimeout),
                initialValue = null
            )
            scheduleFlowsCache[stationId] = scheduleFlow
            screenModelScope.launch(Dispatchers.IO) {
                syncSchedule.await(stationId)
            }
            return scheduleFlow
        }
        return cache
    }

    fun refreshAllStations() {
        screenModelScope.launch {
            _syncScheduleResult.update { SyncSchedule.Result.Loading }
            try {
                favoriteStations.value.map { station ->
                    async { syncSchedule.await(station.id) }
                }.awaitAll()
            } finally {
                _syncScheduleResult.update { SyncSchedule.Result.Success }
            }
        }
    }

    fun onTabFocused(stationId: String) {
        _focusedStationId.update { stationId }
    }

}
