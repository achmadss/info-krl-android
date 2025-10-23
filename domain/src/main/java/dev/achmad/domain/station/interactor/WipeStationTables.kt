package dev.achmad.domain.station.interactor

import dev.achmad.domain.station.repository.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WipeStationTables(
    private val stationRepository: StationRepository
) {

    suspend fun await() {
        withContext(Dispatchers.IO) {
            stationRepository.deleteAll()
        }
    }

}