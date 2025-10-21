package dev.achmad.domain.usecase.station

import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.flow.Flow

class GetStation(
    private val stationRepository: StationRepository
) {

    fun subscribe(): Flow<List<Station>> {
        return stationRepository.subscribeAll()
    }

    fun subscribe(favorite: Boolean?): Flow<List<Station>> {
        return stationRepository.subscribeAll(favorite = favorite)
    }

    fun subscribe(stationId: String): Flow<Station?> {
        return stationRepository.subscribeSingle(stationId)
    }

    suspend fun await(): List<Station> {
        return stationRepository.awaitAll()
    }

    suspend fun await(favorite: Boolean?): List<Station> {
        return stationRepository.awaitAll(favorite = favorite)
    }

    suspend fun await(stationId: String): Station? {
        return stationRepository.awaitSingle(stationId)
    }

}
