package dev.achmad.domain.station.interactor

import dev.achmad.domain.station.repository.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SyncStation(
    private val stationRepository: StationRepository
) {
    suspend fun await(
        checkShouldSync: Boolean = true
    ): Result {
        return try {
            if (checkShouldSync && !shouldSync()) {
                return Result.AlreadySynced
            }
            val stations = stationRepository.fetch()
            stationRepository.store(stations)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun subscribe(): Flow<Result> = flow {
        emit(Result.Loading)
        emit(await())
    }.flowOn(Dispatchers.IO)

    private suspend fun shouldSync(): Boolean {
        return withContext(Dispatchers.IO) {
            val stations = stationRepository.awaitAll()
            return@withContext stations.isEmpty()
        }
    }

    sealed interface Result {
        data object Success : Result
        data object Loading : Result
        data object AlreadySynced : Result
        data class Error(val error: Throwable) : Result
    }

}
