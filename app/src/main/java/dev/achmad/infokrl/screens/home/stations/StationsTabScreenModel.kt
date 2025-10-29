package dev.achmad.infokrl.screens.home.stations

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.domain.station.interactor.GetStation
import dev.achmad.domain.station.interactor.ReorderFavoriteStations
import dev.achmad.domain.station.interactor.SyncStation
import dev.achmad.domain.station.interactor.ToggleFavoriteStation
import dev.achmad.domain.station.model.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StationsTabScreenModel(
    private val getStation: GetStation = inject(),
    private val syncStation: SyncStation = inject(),
    private val toggleFavoriteStation: ToggleFavoriteStation = inject(),
    private val reorderFavoriteStations: ReorderFavoriteStations = inject(),
): ScreenModel {

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery = _searchQuery.asStateFlow()

    private val _syncStationResult = MutableStateFlow<SyncStation.Result?>(null)
    val syncStationResult = _syncStationResult.asStateFlow()

    private val dbStations = getStation.subscribeAll().stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val stations: StateFlow<List<Station>?> = combine(
        _searchQuery,
        dbStations
    ) { query, dbList ->
        val krlStations = dbList?.filter { it.type == Station.Type.KRL }
        when {
            query.isNullOrBlank() -> krlStations
            else -> krlStations?.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    fun search(query: String?) = _searchQuery.update { query }

    fun toggleFavorite(station: Station) {
        screenModelScope.launch(Dispatchers.IO) {
            when (val result = toggleFavoriteStation.await(station)) {
                is ToggleFavoriteStation.Result.Success -> {
                    if (station.favorite) {
                        val remainingFavorites = getStation.awaitAll(favorite = true)
                            .sortedBy { it.favoritePosition }
                        if (remainingFavorites.isNotEmpty()) {
                            when (val reorderResult = reorderFavoriteStations.await(remainingFavorites)) {
                                is ReorderFavoriteStations.Result.Error -> {
                                    reorderResult.error.printStackTrace()
                                }
                                else -> Unit
                            }
                        }
                    }
                }
                is ToggleFavoriteStation.Result.Error -> {
                    result.error.printStackTrace()
                }
            }
        }
    }

    fun reorderFavorite(station: Station, newPosition: Int) {
        screenModelScope.launch {
            // Get fresh data directly from database
            val favorites = getStation.awaitAll(favorite = true)
                .sortedBy { it.favoritePosition }
                .toMutableList()

            val currentIndex = favorites.indexOfFirst { it.id == station.id }
            if (currentIndex == -1) return@launch

            // Reorder in memory
            favorites.add(newPosition, favorites.removeAt(currentIndex))

            // Update positions in database
            withContext(Dispatchers.IO) {
                when (val result = reorderFavoriteStations.await(favorites)) {
                    is ReorderFavoriteStations.Result.Error -> {
                        result.error.printStackTrace()
                    }
                    else -> Unit
                }
            }
        }
    }

    fun fetchStations() {
        screenModelScope.launch {
            syncStation.subscribe().collect {
                _syncStationResult.value = it
            }
        }
    }
}
