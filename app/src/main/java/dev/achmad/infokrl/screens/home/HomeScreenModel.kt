package dev.achmad.infokrl.screens.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.core.di.util.injectContext
import dev.achmad.core.util.TimeTicker
import dev.achmad.domain.schedule.model.Schedule
import dev.achmad.domain.station.model.Station
import dev.achmad.domain.schedule.interactor.GetSchedule
import dev.achmad.domain.station.interactor.GetStation
import dev.achmad.infokrl.util.etaString
import dev.achmad.infokrl.work.SyncScheduleJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

private const val fetchScheduleFinishDelay = 0L

data class DepartureGroup(
    val station: Station,
    val scheduleGroup: StateFlow<List<ScheduleGroup>?>
) {
    data class ScheduleGroup(
        val destinationStation: Station,
        val schedules: List<UISchedule>
    ) {
        data class UISchedule(
            val schedule: Schedule,
            val eta: String,
        )
    }
}

class HomeScreenModel(
    private val getSchedule: GetSchedule = inject(),
    private val getStation: GetStation = inject(),
): ScreenModel {

    private val scheduleFlowsCache = mutableMapOf<String, StateFlow<List<Schedule>?>>()
    private val _focusedStationId = MutableStateFlow<String?>(null)
    val focusedStationId = _focusedStationId.asStateFlow()

    private val _filterFutureSchedulesOnly = MutableStateFlow(true)
    val filterFutureSchedulesOnly = _filterFutureSchedulesOnly.asStateFlow()

    private val tick = TimeTicker(TimeTicker.TickUnit.MINUTE).ticks
        .distinctUntilChanged()
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LocalDateTime.now()
        )

    private val stations = getStation.subscribe()
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val favoriteStations = getStation.subscribe(favorite = true)
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val departureGroups = combine(
        stations,
        favoriteStations,
        _filterFutureSchedulesOnly,
    ) { stations, favorites, _ ->
        favorites
            .sortedBy { it.favoritePosition }
            .map { favorite ->
                DepartureGroup(
                    station = favorite,
                    scheduleGroup = createScheduleGroupFlow(favorite.id, stations)
                )
            }
    }.distinctUntilChanged().stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Creates a reactive flow that combines schedules and time ticker for a station.
     * Emits updated schedule groups whenever schedules or time changes.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createScheduleGroupFlow(
        stationId: String,
        stations: List<Station>
    ): StateFlow<List<DepartureGroup.ScheduleGroup>?> {
        val scheduleFlow = getScheduleFlow(stationId)

        return scheduleFlow
            .flatMapLatest { schedules ->
                if (schedules.isNullOrEmpty()) {
                    return@flatMapLatest flowOf(null)
                }

                // Combine schedules, tick, and filter setting
                combine(
                    flowOf(schedules),
                    tick,
                    _filterFutureSchedulesOnly,
                ) { currentSchedules, time, filterFutureOnly ->
                    // Compute schedule groups off main thread
                    withContext(Dispatchers.Default) {
                        computeScheduleGroups(
                            schedules = currentSchedules,
                            stations = stations,
                            currentTime = time ?: LocalDateTime.now(),
                            filterFutureOnly = filterFutureOnly
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
        filterFutureOnly: Boolean,
    ): List<DepartureGroup.ScheduleGroup> {
        // Group all schedules by destination
        val schedulesByDestination = schedules.groupBy { it.stationDestinationId }
        return schedulesByDestination.mapNotNull { (destinationId, schedulesForDest) ->
            val destinationStation = stations.firstOrNull { it.id == destinationId }
            destinationStation?.let {
                // Filter schedules based on time if needed
                val filteredSchedules = if (filterFutureOnly) {
                    schedulesForDest.filter { it.departsAt.isAfter(currentTime) }
                } else {
                    schedulesForDest
                }

                val sortedSchedules = filteredSchedules.sortedBy { it.departsAt }

                // Map schedules to UI models
                val uiSchedules = sortedSchedules.map { schedule ->
                    DepartureGroup.ScheduleGroup.UISchedule(
                        schedule = schedule,
                        eta = etaString(
                            context = injectContext(),
                            now = currentTime,
                            target = schedule.departsAt
                        ),
                    )
                }

                // Return schedule group if there are schedules
                if (uiSchedules.isNotEmpty()) {
                    DepartureGroup.ScheduleGroup(
                        destinationStation = destinationStation,
                        schedules = uiSchedules
                    )
                } else null
            }
        }.sortedBy { scheduleGroup ->
            // Sort by the line of the first schedule
            scheduleGroup.schedules.firstOrNull()?.schedule?.line
        }
    }

    private fun getScheduleFlow(stationId: String): StateFlow<List<Schedule>?> {
        return scheduleFlowsCache.getOrPut(stationId) {
            getSchedule.subscribe(stationId).stateIn(
                scope = screenModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
        }
    }

    /**
     * Fetches schedules for all favorite stations that need syncing.
     * Typically called on app start or when returning to the home screen.
     */
    fun fetchSchedules(
        manualFetch: Boolean = false
    ) {
        screenModelScope.launch(Dispatchers.IO) {
            favoriteStations.value.forEach { favorite ->
                val stationId = favorite.id
                if (manualFetch) {
                    SyncScheduleJob.startNow(
                        context = injectContext(),
                        stationId = stationId,
                        finishDelay = fetchScheduleFinishDelay
                    )
                } else {
                    SyncScheduleJob.start(
                        context = injectContext(),
                        stationId = stationId,
                        finishDelay = fetchScheduleFinishDelay
                    )
                }
            }
        }
    }

    /**
     * Fetches schedule for a specific station.
     * Used for pull-to-refresh functionality on individual tabs.
     */
    fun fetchScheduleForStation(
        stationId: String,
        manualFetch: Boolean = false
    ) {
        screenModelScope.launch(Dispatchers.IO) {
            if (manualFetch) {
                SyncScheduleJob.startNow(
                    context = injectContext(),
                    stationId = stationId,
                    finishDelay = fetchScheduleFinishDelay
                )
            } else {
                SyncScheduleJob.start(
                    context = injectContext(),
                    stationId = stationId,
                    finishDelay = fetchScheduleFinishDelay
                )
            }
        }
    }

    /**
     * Toggles the filter for showing only future schedules.
     * When false, shows all schedules including past ones (useful for testing).
     */
    fun toggleFilterFutureSchedules() {
        _filterFutureSchedulesOnly.update { !it }
    }

    /**
     * Called when a station tab is focused.
     * Updates the focused station ID.
     */
    fun onTabFocused(stationId: String) {
        _focusedStationId.update { stationId }
    }

}