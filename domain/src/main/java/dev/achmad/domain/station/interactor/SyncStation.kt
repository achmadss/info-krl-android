package dev.achmad.domain.station.interactor

import dev.achmad.domain.station.repository.StationRepository

class SyncStation(
    private val stationRepository: StationRepository
) {
    suspend fun await(): Result {
        return try {
            val stations = stationRepository.fetch()
            stationRepository.store(stations)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    sealed interface Result {
        data object Success : Result
        data class Error(val error: Throwable) : Result
    }

}
