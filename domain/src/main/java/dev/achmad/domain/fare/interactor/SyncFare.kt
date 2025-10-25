package dev.achmad.domain.fare.interactor

import dev.achmad.domain.fare.model.Fare
import dev.achmad.domain.fare.repository.FareRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SyncFare(
    private val fareRepository: FareRepository
) {
    suspend fun await(
        originStationId: String,
        destinationStationId: String,
        checkShouldSync: Boolean = true
    ): Result {
        return withContext(Dispatchers.IO) {
            try {
                if (checkShouldSync && !shouldSync(originStationId, destinationStationId)) {
                    return@withContext Result.AlreadySynced
                }
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

    fun subscribe(
        originStationId: String,
        destinationStationId: String,
    ): Flow<Result> = flow {
        emit(Result.Loading)
        emit(await(originStationId, destinationStationId))
    }.flowOn(Dispatchers.IO)

    private suspend fun shouldSync(
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
        data object Loading : Result
        data object AlreadySynced : Result
        data class Error(val error: Throwable) : Result
    }
}