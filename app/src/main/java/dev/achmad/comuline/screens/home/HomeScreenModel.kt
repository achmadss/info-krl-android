package dev.achmad.comuline.screens.home

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.comuline.util.etaString
import dev.achmad.comuline.work.SyncScheduleJob
import dev.achmad.core.di.util.inject
import dev.achmad.core.util.TimeTicker
import dev.achmad.domain.model.Schedule
import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.ScheduleRepository
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class DestinationGroup(
    val station: Station,
    val scheduleGroup: StateFlow<List<ScheduleGroup>?>
) {
    data class ScheduleGroup(
        val destinationStation: Station,
        val schedules: List<Pair<Schedule, String>>
    )
}

class HomeScreenModel(
    private val stationRepository: StationRepository = inject(),
    private val scheduleRepository: ScheduleRepository = inject(),
): ScreenModel {

    private val minuteTick = TimeTicker(TimeTicker.TickUnit.MINUTE).ticks.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private val stations = stationRepository.stations
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val favoriteStations = stationRepository.favoriteStations
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val destinationGroups = combine(
        stations,
        favoriteStations,
        minuteTick,
    ) { stations, favorites, minuteTick ->
        favorites.map { favorite ->
            DestinationGroup(
                station = favorite,
                scheduleGroup = scheduleRepository.subscribeByStationId(
                    stationId = favorite.id,
                    skipPastSchedule = true
                ).map { schedule ->
                    schedule
                        .groupBy { it.stationDestinationId }
                        .mapNotNull { (stationId, schedulesForStation) ->
                            val station = stations.firstOrNull { it.id == stationId }
                            station?.let {
                                DestinationGroup.ScheduleGroup(
                                    destinationStation = it,
                                    schedules = schedulesForStation
                                        .sortedBy { it.departsAt }
                                        .map {
                                            Pair(
                                                first = it,
                                                second = etaString(
                                                    now = minuteTick ?: LocalDateTime.now(),
                                                    target = it.departsAt
                                                )
                                            )
                                        },
                                )
                            }
                        }
                }.stateIn(
                    scope = screenModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = null
                )
            )
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val focusedStationId = MutableStateFlow<String?>(null)

    fun startAutoRefresh(context: Context) {
        screenModelScope.launch {
            minuteTick.collect { it?.let { refresh(context) } }
        }
    }

    fun onTabFocused(
        context: Context,
        stationId: String
    ) {
        focusedStationId.update { stationId }
        refresh(context)
    }

    fun refresh(
        context: Context,
    ) {
        focusedStationId.value?.let {
            SyncScheduleJob.start(
                context = context,
                stationId = it,
                finishDelay = 1000 // add delay for UI better UX
            )
        }
    }

}