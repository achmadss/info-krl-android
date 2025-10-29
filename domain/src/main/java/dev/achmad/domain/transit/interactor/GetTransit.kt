package dev.achmad.domain.transit.interactor

import dev.achmad.domain.transit.model.Transit
import dev.achmad.domain.transit.repository.TransitRepository
import kotlinx.coroutines.flow.Flow

class GetTransit(
    private val transitRepository: TransitRepository
) {

    fun subscribe(
        originStationId: String,
        destinationStationId: String
    ): Flow<Transit?> {
        return transitRepository.subscribe(
            originStationId = originStationId,
            destinationStationId = destinationStationId
        )
    }

    suspend fun awaitSingle(
        originStationId: String,
        destinationStationId: String
    ): Transit? {
        return transitRepository.await(
            originStationId = originStationId,
            destinationStationId = destinationStationId
        )
    }

}