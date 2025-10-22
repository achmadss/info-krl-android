package dev.achmad.domain.station.interactor

import dev.achmad.domain.station.model.Station
import dev.achmad.domain.station.repository.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ReorderFavoriteStations(
    private val stationRepository: StationRepository
) {
    private val mutex = Mutex()

    suspend fun await(stations: List<Station>): Result {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                try {
                    val reorderedStations = stations
                        .mapIndexed { index, station ->
                            station.copy(favoritePosition = index)
                        }
                    val hasChanged = stations
                        .zip(reorderedStations)
                        .any { (old, new) ->
                            old.favoritePosition != new.favoritePosition
                        }
                    if (!hasChanged) return@withLock Result.Unchanged
                    stationRepository.update(reorderedStations)
                    Result.Success
                } catch (e: Exception) {
                    Result.Error(e)
                }
            }
        }
    }

    sealed interface Result {
        data object Success : Result
        data object Unchanged : Result
        data class Error(val error: Throwable) : Result
    }

}
