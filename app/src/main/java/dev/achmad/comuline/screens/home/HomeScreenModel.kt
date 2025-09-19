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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

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
        )
    }
}

class HomeScreenModel(
    private val stationRepository: StationRepository = inject(),
    private val scheduleRepository: ScheduleRepository = inject(),
): ScreenModel {

    private val scheduleFlowsCache = mutableMapOf<String, StateFlow<List<Schedule>?>>()
    private val _focusedStationId = MutableStateFlow<String?>(null)


    private val tick = TimeTicker(TimeTicker.TickUnit.MINUTE).ticks.stateIn(
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
    ) { stations, favorites ->
        favorites
            .sortedBy { it.favoritePosition }
            .map { favorite ->
                val scheduleFlow = getScheduleFlow(favorite.id)
                val scheduleGroupFlow = combine(
                    scheduleFlow,
                    tick
                ) { schedule, minuteTick ->
                    if (schedule != null && minuteTick != null) {
                        mapToScheduleGroup(schedule, stations, minuteTick)
                    } else null
                }.stateIn(
                    scope = screenModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = null
                )
                DestinationGroup(
                    station = favorite,
                    scheduleGroup = scheduleGroupFlow
                )
            }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private fun getScheduleFlow(stationId: String): StateFlow<List<Schedule>?> {
        return scheduleFlowsCache.getOrPut(stationId) {
            scheduleRepository.subscribeByStationId(
                stationId = stationId,
            ).stateIn(
                scope = screenModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )
        }
    }

    private fun mapToScheduleGroup(
        schedule: List<Schedule>,
        stations: List<Station>,
        minuteTick: LocalDateTime
    ) = schedule
        .groupBy { it.stationDestinationId }
        .mapNotNull { (stationId, schedulesForStation) ->
            val station = stations.firstOrNull { it.id == stationId }
            station?.let {
                DestinationGroup.ScheduleGroup(
                    destinationStation = it,
                    schedules = mapToUISchedule(schedulesForStation, minuteTick),
                )
            }
        }

    private fun mapToUISchedule(
        schedulesForStation: List<Schedule>,
        minuteTick: LocalDateTime
    ) = schedulesForStation
        .sortedBy { it.departsAt }
        .mapNotNull {
            if (it.departsAt.isAfter(minuteTick)) {
                DestinationGroup.ScheduleGroup.UISchedule(
                    schedule = it,
                    eta = etaString(
                        now = minuteTick,
                        target = it.departsAt
                    )
                )
            } else null
        }

    fun fetchSchedules(context: Context) {
        _focusedStationId.value?.let {
            if (SyncScheduleJob.shouldSync(it)) {
                val finishDelay = 500L // add delay for better UX
                SyncScheduleJob.startNow(
                    context = context,
                    stationId = it,
                    finishDelay = finishDelay
                )
            }
        }
    }

    fun onTabFocused(
        context: Context,
        stationId: String,
    ) {
        _focusedStationId.update { stationId }
        fetchSchedules(context)
    }

}