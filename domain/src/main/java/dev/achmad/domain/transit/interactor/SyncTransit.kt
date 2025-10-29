package dev.achmad.domain.transit.interactor

import dev.achmad.domain.transit.repository.TransitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class SyncTransit(
    private val transitRepository: TransitRepository
) {

    suspend fun await(
        originStationId: String,
        destinationStationId: String,
        checkShouldSync: Boolean = true
    ): Result {
        return withContext(Dispatchers.IO) {
            if (checkShouldSync && !shouldSync(originStationId, destinationStationId)) Result.AlreadySynced
            try {
                val transit = transitRepository.fetch(
                    originStationId = originStationId,
                    destinationStationId = destinationStationId
                )
                transitRepository.store(transit)
                Result.Success
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    fun subscribe(
        originStationId: String,
        destinationStationId: String,
        checkShouldSync: Boolean = true
    ): Flow<Result> = flow {
        emit(Result.Loading)
        emit(await(originStationId, destinationStationId, checkShouldSync))
    }

    private suspend fun shouldSync(
        originStationId: String,
        destinationStationId: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val transit = transitRepository.await(
                originStationId = originStationId,
                destinationStationId = destinationStationId
            )
            return@withContext (transit == null)
        }
    }

    sealed interface Result {
        data object Success : Result
        data object Loading : Result
        data object AlreadySynced : Result
        data class Error(val error: Throwable) : Result
    }

}