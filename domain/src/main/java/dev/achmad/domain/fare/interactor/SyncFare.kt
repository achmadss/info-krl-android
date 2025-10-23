package dev.achmad.domain.fare.interactor

import dev.achmad.domain.fare.repository.FareRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncFare(
    private val fareRepository: FareRepository
) {
    suspend fun await(
        originStationId: String,
        destinationStationId: String,
    ): Result {
        return withContext(Dispatchers.IO) {
            try {
                val fare = fareRepository.fetch(
                    originStationId = originStationId,
                    destinationStationId = destinationStationId
                )
                fareRepository.store(fare)
                Result.Success
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    suspend fun shouldSync(
        originStationId: String,
        destinationStationId: String,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val fare = fareRepository.await(originStationId, destinationStationId)
            return@withContext fare == null
        }
    }

    sealed interface Result {
        data object Success : Result
        data class Error(val error: Throwable) : Result
    }
}