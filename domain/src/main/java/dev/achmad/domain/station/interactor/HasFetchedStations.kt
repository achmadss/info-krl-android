package dev.achmad.domain.station.interactor

import dev.achmad.domain.station.repository.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HasFetchedStations(
    private val stationRepository: StationRepository
) {
    fun subscribe(): Flow<Boolean> {
        return stationRepository.subscribeAll()
            .map { it.isNotEmpty() }
    }

    suspend fun await(): Boolean {
        return withContext(Dispatchers.IO) {
            stationRepository.awaitAll().isNotEmpty()
        }
    }
}