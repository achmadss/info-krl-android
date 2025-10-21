package dev.achmad.infokrl.screens.schedules.timelinescreen

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.core.di.util.injectContext
import dev.achmad.core.util.TimeTicker
import dev.achmad.domain.model.Route
import dev.achmad.domain.usecase.route.GetRoute
import dev.achmad.infokrl.work.SyncRouteJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class TimelineGroup(
    val currentRoute: Route,
    val currentTime: LocalDateTime
)

class TimelineScreenModel(
    private val trainId: String,
    private val getRoute: GetRoute = inject(),
): ScreenModel {

    private val routeFlowsCache = mutableMapOf<String, StateFlow<Route?>>()

    private val tick = TimeTicker(TimeTicker.TickUnit.MINUTE).ticks
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val timelineGroup: StateFlow<TimelineGroup?> = combine(
        tick,
        getRouteFlow(trainId)
    ) { tickValue, currentRoute ->
        when {
            currentRoute == null -> {
                fetchRoute(listOf(trainId))
                null
            }
            else -> {
                TimelineGroup(
                    currentRoute = currentRoute,
                    currentTime = LocalDateTime.now()
                )
            }
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private fun getRouteFlow(trainId: String): StateFlow<Route?> {
        return routeFlowsCache.getOrPut(trainId) {
            getRoute.subscribe(trainId).stateIn(
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
        fetchRoute(listOf(trainId))
    }
}
