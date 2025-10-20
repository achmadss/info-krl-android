package dev.achmad.infokrl.screens.stations

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.StationRepository
import dev.achmad.infokrl.base.ApplicationPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StationsScreenModel(
    private val stationRepository: StationRepository = inject(),
    private val applicationPreference: ApplicationPreference = inject(),
): ScreenModel {

    // Mutex to prevent race conditions between reorder and toggle operations
    private val favoritesMutex = Mutex()

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery = _searchQuery.asStateFlow()

    private val dbStations = stationRepository.subscribeAll()
        .let {
            if (!applicationPreference.hasFetchedStations().get()) it.drop(1)
            else it
        }
        .stateIn(
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
            favoritesMutex.withLock {
                if (!station.favorite) {
                    val currentFavorites = stationRepository.awaitAllFavorites()
                    val updatedStation = station.copy(
                        favorite = true,
                        favoritePosition = currentFavorites.size
                    )
                    stationRepository.updateFavorite(updatedStation)
                } else {
                    stationRepository.toggleFavorite(station)
                    val remainingFavorites = stationRepository
                        .awaitAllFavorites()
                        .sortedBy { it.favoritePosition }
                    if (remainingFavorites.isNotEmpty()) {
                        stationRepository.reorderFavorites(remainingFavorites)
                    }
                }
            }
        }
    }

    fun reorderFavorite(station: Station, newPosition: Int) {
        screenModelScope.launch {
            favoritesMutex.withLock {
                // Get fresh data directly from database
                val favorites = stationRepository.awaitAllFavorites()
                    .sortedBy { it.favoritePosition }
                    .toMutableList()

                val currentIndex = favorites.indexOfFirst { it.id == station.id }
                if (currentIndex == -1) return@withLock

                // Reorder in memory
                favorites.add(newPosition, favorites.removeAt(currentIndex))

                // Update positions in database
                stationRepository.reorderFavorites(favorites)
            }
        }
    }
}