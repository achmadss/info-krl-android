package dev.achmad.domain.station.interactor

import dev.achmad.domain.station.model.Station
import dev.achmad.domain.station.repository.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GetStation(
    private val stationRepository: StationRepository
) {

    fun subscribeAll(): Flow<List<Station>> {
        return stationRepository.subscribeAll()
    }

    fun subscribeAll(favorite: Boolean?): Flow<List<Station>> {
        return stationRepository.subscribeAll(favorite = favorite)
    }

    fun subscribeSingle(stationId: String): Flow<Station?> {
        return stationRepository.subscribeSingle(stationId)
    }

    fun subscribeMultiple(stationIds: List<String>): Flow<List<Station>> {
        return stationRepository.subscribeMultiple(stationIds)
    }

    suspend fun awaitAll(): List<Station> {
        return withContext(Dispatchers.IO) {
            stationRepository.awaitAll()
        }
    }

    suspend fun awaitAll(favorite: Boolean?): List<Station> {
        return withContext(Dispatchers.IO) {
            stationRepository.awaitAll(favorite = favorite)
        }
    }

    suspend fun awaitSingle(stationId: String): Station? {
        return withContext(Dispatchers.IO) {
            stationRepository.awaitSingle(stationId)
        }
    }

}
