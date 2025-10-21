package dev.achmad.infokrl.screens.stations

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.domain.station.model.Station
import dev.achmad.domain.station.interactor.ReorderFavoriteStations
import dev.achmad.domain.station.interactor.GetStation
import dev.achmad.domain.station.interactor.ToggleFavoriteStation
import dev.achmad.domain.station.interactor.HasFetchedStations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StationsScreenModel(
    private val getStation: GetStation = inject(),
    private val toggleFavoriteStation: ToggleFavoriteStation = inject(),
    private val reorderFavoriteStations: ReorderFavoriteStations = inject(),
    private val hasFetchedStations: HasFetchedStations = inject(),
): ScreenModel {

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery = _searchQuery.asStateFlow()

    private val dbStations = combine(
        getStation.subscribe(),
        hasFetchedStations.subscribe()
    ) { stations, hasFetched ->
        if (hasFetched) stations else null
    }.stateIn(
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
        screenModelScope.launch {
            when (val result = toggleFavoriteStation.await(station)) {
                is ToggleFavoriteStation.Result.Success -> {
                    if (station.favorite) {
                        val remainingFavorites = getStation.await(favorite = true)
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
            val favorites = getStation.await(favorite = true)
                .sortedBy { it.favoritePosition }
                .toMutableList()

            val currentIndex = favorites.indexOfFirst { it.id == station.id }
            if (currentIndex == -1) return@launch

            // Reorder in memory
            favorites.add(newPosition, favorites.removeAt(currentIndex))

            // Update positions in database
            when (val result = reorderFavoriteStations.await(favorites)) {
                is ReorderFavoriteStations.Result.Success -> {
                    // Success
                }
                is ReorderFavoriteStations.Result.Unchanged -> {
                    // Nothing changed
                }
                is ReorderFavoriteStations.Result.Error -> {
                    result.error.printStackTrace()
                }
            }
        }
    }
}