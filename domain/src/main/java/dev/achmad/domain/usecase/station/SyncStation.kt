package dev.achmad.domain.usecase.station

import dev.achmad.domain.repository.StationRepository

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
