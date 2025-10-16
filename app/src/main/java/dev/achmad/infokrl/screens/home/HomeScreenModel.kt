package dev.achmad.infokrl.screens.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.infokrl.util.etaString
import dev.achmad.infokrl.work.SyncRouteJob
import dev.achmad.infokrl.work.SyncScheduleJob
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

private const val fetchScheduleFinishDelay = 0L
private const val fetchRouteFinishDelay = 0L

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
    private val _routeUpdateTrigger = MutableStateFlow(0L)
    private val inFlightRouteFetches = mutableSetOf<String>()
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
     * Creates a reactive flow that combines schedules, routes, and time ticker for a station.
     * Emits updated schedule groups whenever schedules, routes, or time changes.
     * Optimized to reduce recomposition overhead.
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

                // Get train IDs for routes we need and ensure flows exist
                val currentTime = LocalDateTime.now()
                val trainIds = extractFirstTrainIds(schedules, currentTime, _filterFutureSchedulesOnly.value)
                trainIds.forEach { trainId -> getRouteFlow(trainId) }

                // Combine schedules, tick, filter setting, and route update trigger
                combine(
                    flowOf(schedules),
                    tick,
                    _filterFutureSchedulesOnly,
                    _routeUpdateTrigger
                ) { currentSchedules, time, filterFutureOnly, _ ->
                    // Recompute trainIds based on current time to handle trains that are now in the past
                    val currentTrainIds = extractFirstTrainIds(
                        schedules = currentSchedules,
                        currentTime = time ?: LocalDateTime.now(),
                        filterFutureOnly = filterFutureOnly
                    )

                    // Ensure route flows exist and check which routes are missing
                    val missingRouteTrainIds = currentTrainIds.filter { trainId ->
                        val flow = getRouteFlow(trainId)
                        flow.value == null
                    }

                    // Fetch missing routes if needed
                    if (missingRouteTrainIds.isNotEmpty()) {
                        fetchRoute(missingRouteTrainIds, stationId, manualFetch = false)
                    }

                    // Compute schedule groups off main thread
                    withContext(Dispatchers.Default) {
                        computeScheduleGroups(
                            favoriteStationId = stationId,
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
        favoriteStationId: String,
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

                // Get the first train's ID to fetch route
                val firstTrainId = sortedSchedules.firstOrNull()?.trainId

                // Get route if available (non-blocking)
                val route = firstTrainId?.let { trainId ->
                    routeFlowsCache[trainId]?.value
                }

                // Calculate stops count
                val stopStationIds = route?.stops?.map { it.stationId }
                val bstStationsIds = listOf("SUDB", "DU", "RW", "BPR")
                val stopsCount = stopStationIds
                    ?.indexOf(favoriteStationId)
                    ?.takeIf { it != -1 }
                    ?.let { index -> stopStationIds.drop(index + 1) }
                    ?.let { remainingStops ->
                        if (route.line.contains("BST")) {
                            remainingStops.filter { stationId -> stationId in bstStationsIds }
                        } else remainingStops
                    }
                    ?.size

                // Map schedules to UI models
                val uiSchedules = sortedSchedules.map { schedule ->
                    DepartureGroup.ScheduleGroup.UISchedule(
                        schedule = schedule,
                        eta = etaString(
                            context = injectContext(),
                            now = currentTime,
                            target = schedule.departsAt
                        ),
                        stops = stopsCount
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
     * Checks if routes are missing from cache and forces fetch if needed.
     */
    private fun fetchRoutesForStation(
        stationId: String,
        manualFetch: Boolean = false,
    ) {
        val schedules = scheduleFlowsCache[stationId]?.value ?: return
        if (schedules.isEmpty()) return

        val currentTime = LocalDateTime.now()
        val trainIds = extractFirstTrainIds(schedules, currentTime, _filterFutureSchedulesOnly.value)

        // Ensure route flows are created for all train IDs
        trainIds.forEach { trainId -> getRouteFlow(trainId) }

        // Check if any routes are missing from cache
        val hasAnyMissingRoutes = trainIds.any { trainId ->
            routeFlowsCache[trainId]?.value == null
        }

        // Force fetch if routes are missing, otherwise respect manualFetch flag
        val shouldForceFetch = hasAnyMissingRoutes || manualFetch

        fetchRoute(trainIds, stationId, shouldForceFetch)
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
        // Filter out trains that are already being fetched
        val trainIdsToFetch = synchronized(inFlightRouteFetches) {
            trainIds.filter { trainId ->
                !inFlightRouteFetches.contains(trainId)
            }.also { filtered ->
                // Mark these trains as being fetched
                inFlightRouteFetches.addAll(filtered)
            }
        }

        if (trainIdsToFetch.isEmpty()) return

        // Launch a coroutine to remove from in-flight set after fetch completes
        screenModelScope.launch(Dispatchers.IO) {
            try {
                if (manualFetch) {
                    SyncRouteJob.startNow(
                        context = injectContext(),
                        trainIds = trainIdsToFetch,
                        stationId = stationId,
                        finishDelay = fetchRouteFinishDelay
                    )
                } else {
                    SyncRouteJob.start(
                        context = injectContext(),
                        trainIds = trainIdsToFetch,
                        stationId = stationId,
                        finishDelay = fetchRouteFinishDelay
                    )
                }
                // Wait a bit for the job to complete (finishDelay + buffer)
                kotlinx.coroutines.delay(fetchRouteFinishDelay + 500)
            } finally {
                // Remove from in-flight set
                synchronized(inFlightRouteFetches) {
                    inFlightRouteFetches.removeAll(trainIdsToFetch.toSet())
                }
            }
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