package dev.achmad.infokrl.screens.timeline

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.util.TimeTicker
import dev.achmad.core.util.inject
import dev.achmad.domain.route.interactor.GetRoute
import dev.achmad.domain.route.interactor.SyncRoute
import dev.achmad.domain.route.model.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val syncRoute: SyncRoute = inject(),
): ScreenModel {

    private val _syncRouteResult = MutableStateFlow<SyncRoute.Result?>(null)
    val syncRouteResult = _syncRouteResult.asStateFlow()

    init {
        fetchRoute(trainId)
    }

    private val tick = TimeTicker(TimeTicker.TickUnit.MINUTE).ticks
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val route = getRoute.subscribe(trainId).stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val timelineGroup: StateFlow<TimelineGroup?> = combine(
        tick,
        route
    ) { tickValue, currentRoute ->
        when {
            currentRoute == null -> { null }
            else -> {
                TimelineGroup(
                    currentRoute = currentRoute,
                    currentTime = tickValue ?: LocalDateTime.now()
                )
            }
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private fun fetchRoute(trainId: String) {
        screenModelScope.launch {
            syncRoute.subscribe(trainId).collect {
                _syncRouteResult.value = it
            }
        }
    }

    fun refresh() {
        fetchRoute(trainId)
    }
}
