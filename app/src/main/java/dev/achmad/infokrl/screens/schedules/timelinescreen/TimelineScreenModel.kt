package dev.achmad.infokrl.screens.schedules.timelinescreen

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.core.di.util.injectContext
import dev.achmad.core.util.TimeTicker
import dev.achmad.domain.model.Route
import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.RouteRepository
import dev.achmad.domain.repository.StationRepository
import dev.achmad.infokrl.work.SyncRouteJob
import dev.achmad.infokrl.util.mergeRouteStops
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class TimelineGroup(
    val originStation: Station,
    val destinationStation: Station,
    val maxStopRoute: Route,
    val currentRoute: Route,
    val currentTime: LocalDateTime
)

class TimelineScreenModel(
    private val trainId: String,
    private val originStationId: String,
    private val destinationStationId: String,
    private val routeRepository: RouteRepository = inject(),
    private val stationRepository: StationRepository = inject()
): ScreenModel {

    private val routeFlowsCache = mutableMapOf<String, StateFlow<Route?>>()

    private val tick = TimeTicker(TimeTicker.TickUnit.MINUTE).ticks.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val timelineGroup: StateFlow<TimelineGroup?> = combine(
        tick,
        getStationFlow(originStationId),
        getStationFlow(destinationStationId),
        getRouteFlow(maxStopsTrainId),
        getRouteFlow(trainId)
    ) { _, originStation, destinationStation, maxStopRoute, currentRoute ->
        when {
            originStation == null -> null
            destinationStation == null -> null
            maxStopRoute == null -> null
            currentRoute == null -> null
            else -> {
                // Merge current route with max stop route to fill in missing stops
                val mergedRoute = mergeRouteStops(
                    currentRoute = currentRoute,
                    maxStopRoute = maxStopRoute
                )

                TimelineGroup(
                    originStation = originStation,
                    destinationStation = destinationStation,
                    maxStopRoute = maxStopRoute,
                    currentRoute = mergedRoute,
                    currentTime = LocalDateTime.now()
                )
            }
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

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
                )
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

    fun refresh() {
        fetchRoute(listOf(trainId, maxStopsTrainId))
    }
}
