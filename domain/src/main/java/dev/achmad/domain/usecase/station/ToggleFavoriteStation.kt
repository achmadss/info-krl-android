package dev.achmad.domain.usecase.station

import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.StationRepository

class ToggleFavoriteStation(
    private val stationRepository: StationRepository
) {

    sealed interface Result {
        data object Success : Result
        data class Error(val error: Throwable) : Result
    }

    suspend fun await(station: Station): Result {
        return try {
            if (station.favorite) {
                stationRepository.unfavorite(station.id)
            } else {
                stationRepository.favorite(station.id)
            }
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

}
