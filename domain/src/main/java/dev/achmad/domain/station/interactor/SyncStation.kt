package dev.achmad.domain.station.interactor

import dev.achmad.domain.station.repository.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class SyncStation(
    private val stationRepository: StationRepository
) {
    suspend fun await(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val stations = stationRepository.fetch()
                stationRepository.store(stations)
                Result.Success
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    fun subscribe(): Flow<Result> = flow {
        withContext(Dispatchers.IO) {
            emit(Result.Loading)
            emit(await())
        }
    }

    sealed interface Result {
        data object Success : Result
        data object Loading : Result
        data class Error(val error: Throwable) : Result
    }

}
