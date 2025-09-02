package dev.achmad.comuline.screens.home

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.comuline.work.RefreshScheduleJob
import dev.achmad.comuline.work.RefreshStationJob
import dev.achmad.core.di.util.inject
import dev.achmad.domain.model.Schedule
import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.ScheduleRepository
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DestinationGroup(
    val station: Station,
    val schedules: StateFlow<List<Schedule>>
)

class HomeScreenModel(
    private val stationRepository: StationRepository = inject(),
    private val scheduleRepository: ScheduleRepository = inject(),
): ScreenModel {

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery = _searchQuery.asStateFlow()

    val favoriteStations = stationRepository.favoriteStations
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val destinationGroups = combine(
        searchQuery,
        favoriteStations,
    ) { query, stations ->
        val filteredStations = when {
            query.isNullOrBlank() -> stations
            else -> stations.filter { it.name.contains(query, ignoreCase = true) }
        }
        filteredStations.map { station ->
            DestinationGroup(
                station = station,
                schedules = scheduleRepository.subscribeByStationId(station.id).stateIn(
                    scope = screenModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = emptyList()
                )
            )
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

//    val destinationGroups = combine(
//        searchQuery,
//        favoriteStations,
//        schedules,
//    ) { query, stations, schedules ->
//        val filteredStations = when {
//            query.isNullOrBlank() -> stations
//            else -> stations.filter { it.name.contains(query, ignoreCase = true) }
//        }
//        val schedulesByStation = schedules.groupBy { it.stationId }
//        filteredStations.map { station ->
//            DestinationGroup(
//                station = station,
//                schedules = schedulesByStation[station.id].orEmpty()
//            )
//        }
//    }.stateIn(
//        scope = screenModelScope,
//        started = SharingStarted.Eagerly,
//        initialValue = emptyList()
//    )

    fun search(query: String?) = _searchQuery.update { query }

    fun refreshStation(context: Context) {
        screenModelScope.launch { RefreshStationJob.start(context) }
    }

    fun refreshSchedule(context: Context) {
        favoriteStations.value.map { station ->
            screenModelScope.launch {
                RefreshScheduleJob.start(context, station.id)
            }
        }
    }

}