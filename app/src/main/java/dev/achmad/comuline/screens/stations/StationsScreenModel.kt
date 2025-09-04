package dev.achmad.comuline.screens.stations

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.comuline.base.ApplicationPreference
import dev.achmad.core.di.util.inject
import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StationsScreenModel(
    private val stationRepository: StationRepository = inject(),
    private val applicationPreference: ApplicationPreference = inject(),
): ScreenModel {

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery = _searchQuery.asStateFlow()

    private val stations = stationRepository.stations
        .let {
            if (!applicationPreference.hasFetchedStations().get()) it.drop(1)
            else it
        }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val filteredStations = combine(
        searchQuery,
        stations,
    ) { query, stations ->
        when {
            query.isNullOrBlank() -> stations
            else -> stations?.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    fun search(query: String?) = _searchQuery.update { query }

    fun toggleFavorite(station: Station) {
        screenModelScope.launch {
            stationRepository.toggleFavorite(station)
        }
    }

}