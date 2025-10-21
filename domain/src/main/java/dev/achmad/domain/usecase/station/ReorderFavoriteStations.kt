package dev.achmad.domain.usecase.station

import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ReorderFavoriteStations(
    private val stationRepository: StationRepository
) {

    private val mutex = Mutex()

    sealed interface Result {
        data object Success : Result
        data object Unchanged : Result
        data class Error(val error: Throwable) : Result
    }

    suspend fun await(stations: List<Station>): Result {
        return mutex.withLock {
            try {
                val reorderedStations = stations.mapIndexed { index, station ->
                    station.copy(favoritePosition = index)
                }

                // Check if anything actually changed
                val hasChanged = stations.zip(reorderedStations).any { (old, new) ->
                    old.favoritePosition != new.favoritePosition
                }

                if (!hasChanged) {
                    return@withLock Result.Unchanged
                }

                stationRepository.update(reorderedStations)
                Result.Success
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

}
