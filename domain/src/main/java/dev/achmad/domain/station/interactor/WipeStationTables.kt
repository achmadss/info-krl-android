package dev.achmad.domain.station.interactor

import dev.achmad.domain.station.repository.StationRepository

class WipeStationTables(
    private val stationRepository: StationRepository
) {

    suspend fun execute() {
        stationRepository.deleteAll()
    }

}