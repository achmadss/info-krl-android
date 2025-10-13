package dev.achmad.comuline.screens.home

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

private const val fetchScheduleFinishDelay = 1000L
private const val fetchRouteFinishDelay = 1000L

data class DestinationGroup(
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
            val stops: Int?,
        )
    }
}

class HomeScreenModel(
    stationRepository: StationRepository = inject(),
    private val scheduleRepository: ScheduleRepository = inject(),
    private val routeRepository: RouteRepository = inject(),
): ScreenModel {

    private val scheduleFlowsCache = mutableMapOf<String, StateFlow<List<Schedule>?>>()
    private val routeFlowsCache = mutableMapOf<String, StateFlow<Route?>>()
    private val _focusedStationId = MutableStateFlow<String?>(null)
    val focusedStationId = _focusedStationId.asStateFlow()

    private val _filterFutureSchedulesOnly = MutableStateFlow(true)
    val filterFutureSchedulesOnly = _filterFutureSchedulesOnly.asStateFlow()

    // Debounce ticker to reduce recomposition frequency
    private val tick = TimeTicker(TimeTicker.TickUnit.MINUTE).ticks
        .distinctUntilChanged()
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LocalDateTime.now()
        )

    private val stations = stationRepository.stations
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val favoriteStations = stationRepository.favoriteStations
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Combines favorite stations with their schedules and routes to create destination groups.
     * Updates reactively when stations, favorites, schedules, routes, or time changes.
     */
    val destinationGroups = combine(
        stations,
        favoriteStations,
        _filterFutureSchedulesOnly,
    ) { stations, favorites, _ ->
        favorites
            .sortedBy { it.favoritePosition }
            .map { favorite ->
                DestinationGroup(
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
     * Creates a reactive flow that combines schedules, routes, and time ticker for a station.
     * Emits updated schedule groups whenever schedules, routes, or time changes.
     * Optimized to reduce recomposition overhead.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createScheduleGroupFlow(
        stationId: String,
        stations: List<Station>
    ): StateFlow<List<DestinationGroup.ScheduleGroup>?> {
        val scheduleFlow = getScheduleFlow(stationId)

        return scheduleFlow
            .flatMapLatest { schedules ->
                if (schedules.isNullOrEmpty()) {
                    return@flatMapLatest flowOf(null)
                }

                // Get train IDs for routes we need
                val currentTime = LocalDateTime.now()
                val trainIds = extractFirstTrainIds(schedules, currentTime, _filterFutureSchedulesOnly.value)

                // Trigger background fetch for routes (non-blocking)
                if (trainIds.isNotEmpty()) {
                    screenModelScope.launch(Dispatchers.IO) {
                        fetchRoute(trainIds, manualFetch = false)
                    }
                }

                // Create route flows for all needed trains
                val routeFlows = trainIds.map { trainId -> getRouteFlow(trainId) }

                // Combine schedules, all route flows, time ticker, and filter setting
                combine(
                    flowOf(schedules),
                    *routeFlows.toTypedArray(),
                    tick,
                    _filterFutureSchedulesOnly
                ) { values ->
                    @Suppress("UNCHECKED_CAST")
                    val currentSchedules = values[0] as List<Schedule>
                    val time = values[values.size - 2] as LocalDateTime
                    val filterFutureOnly = values[values.size - 1] as Boolean

                    // Compute schedule groups off main thread
                    withContext(Dispatchers.Default) {
                        computeScheduleGroups(currentSchedules, stations, time, filterFutureOnly)
                    }
                }
            }
            .distinctUntilChanged()
            .stateIn(
                scope = screenModelScope,
                started = SharingStarted.WhileSubscribed(5000),
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
    ): List<DestinationGroup.ScheduleGroup> {
        // Group all schedules by destination
        val schedulesByDestination = schedules.groupBy { it.stationDestinationId }


        return schedulesByDestination.mapNotNull { (destinationId, schedulesForDest) ->
            val station = stations.firstOrNull { it.id == destinationId }
            station?.let {
                // Filter schedules based on time if needed
                val filteredSchedules = if (filterFutureOnly) {
                    schedulesForDest.filter { it.departsAt.isAfter(currentTime) }
                } else {
                    schedulesForDest
                }
                
                val sortedSchedules = filteredSchedules.sortedBy { it.departsAt }

                // Get the first train's ID to fetch route
                val firstTrainId = sortedSchedules.firstOrNull()?.trainId

                // Get route if available (non-blocking)
                val route = firstTrainId?.let { trainId ->
                    routeFlowsCache[trainId]?.value
                }

                // Map schedules to UI models
                val uiSchedules = sortedSchedules.map { schedule ->
                    DestinationGroup.ScheduleGroup.UISchedule(
                        schedule = schedule,
                        eta = etaString(
                            now = currentTime,
                            target = schedule.departsAt
                        ),
                        stops = route?.stops?.size
                    )
                }

                // Return schedule group if there are schedules
                if (uiSchedules.isNotEmpty()) {
                    DestinationGroup.ScheduleGroup(
                        destinationStation = station,
                        schedules = uiSchedules
                    )
                } else null
            }
        }.sortedBy { scheduleGroup ->
            // Sort by the line of the first schedule
            scheduleGroup.schedules.firstOrNull()?.schedule?.line
        }
    }

    /**
     * Extracts the earliest train ID for each destination from schedules.
     * This ensures we fetch route data for the first train to each destination.
     */
    private fun extractFirstTrainIds(
        schedules: List<Schedule>,
        currentTime: LocalDateTime,
        filterFutureOnly: Boolean
    ): List<String> {
        val filtered = if (filterFutureOnly) {
            schedules.filter { it.departsAt.isAfter(currentTime) }
        } else {
            schedules
        }
        
        return filtered
            .groupBy { it.stationDestinationId }
            .mapNotNull { (_, schedulesForDestination) ->
                schedulesForDestination.minByOrNull { it.departsAt }?.trainId
            }
            .distinct()
    }

    private fun getScheduleFlow(stationId: String): StateFlow<List<Schedule>?> {
        return scheduleFlowsCache.getOrPut(stationId) {
            scheduleRepository.subscribeSingle(
                stationId = stationId,
            ).stateIn(
                scope = screenModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
        }
    }

    private fun getRouteFlow(trainId: String): StateFlow<Route?> {
        return routeFlowsCache.getOrPut(trainId) {
            routeRepository.subscribeSingle(
                trainId = trainId,
            ).stateIn(
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

            // Fetch routes after schedules are loaded
            favoriteStations.value.forEach { favorite ->
                launch {
                    val scheduleFlow = getScheduleFlow(favorite.id)
                    scheduleFlow
                        .filterNotNull()
                        .first { it.isNotEmpty() }
                    fetchRoutesForStation(favorite.id, manualFetch)
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
        manualFetch: Boolean = true
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

            // Also fetch routes for this station after schedules are available
            val scheduleFlow = getScheduleFlow(stationId)
            scheduleFlow
                .filterNotNull()
                .first { it.isNotEmpty() }
            fetchRoutesForStation(stationId, manualFetch)
        }
    }

    /**
     * Fetches routes for the first train to each destination from a station.
     * Only fetches if routes need to be synced according to SyncRouteJob.
     */
    private fun fetchRoutesForStation(
        stationId: String,
        manualFetch: Boolean = false,
    ) {
        val schedules = scheduleFlowsCache[stationId]?.value ?: return
        if (schedules.isEmpty()) return

        val currentTime = LocalDateTime.now()
        val trainIds = extractFirstTrainIds(schedules, currentTime, _filterFutureSchedulesOnly.value)
        screenModelScope.launch(Dispatchers.IO) {
            fetchRoute(trainIds, stationId, manualFetch)
        }
    }

    /**
     * Toggles the filter for showing only future schedules.
     * When false, shows all schedules including past ones (useful for testing).
     */
    fun toggleFilterFutureSchedules() {
        _filterFutureSchedulesOnly.update { !it }
    }

    private fun fetchRoute(
        trainIds: List<String>,
        stationId: String? = null,
        manualFetch: Boolean = false,
    ) {
        if (manualFetch) {
            SyncRouteJob.startNow(
                context = injectContext(),
                trainIds = trainIds,
                stationId = stationId,
                finishDelay = fetchRouteFinishDelay
            )
        } else {
            SyncRouteJob.start(
                context = injectContext(),
                trainIds = trainIds,
                stationId = stationId,
                finishDelay = fetchRouteFinishDelay
            )
        }
    }

    /**
     * Called when a station tab is focused.
     * Updates the focused station ID and triggers route fetching for that station.
     * If schedules aren't loaded yet, waits for them before fetching routes.
     */
    fun onTabFocused(stationId: String) {
        _focusedStationId.update { stationId }
        screenModelScope.launch(Dispatchers.IO) {
            fetchRoutesForStation(stationId)

            // If schedules aren't available yet, wait for them to load then fetch routes
            val scheduleFlow = getScheduleFlow(stationId)
            if (scheduleFlow.value.isNullOrEmpty()) {
                scheduleFlow
                    .filterNotNull()
                    .first { it.isNotEmpty() }
                fetchRoutesForStation(stationId)
            }
        }
    }

}