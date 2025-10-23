package dev.achmad.domain.fare.interactor

import dev.achmad.domain.fare.model.Fare
import dev.achmad.domain.fare.repository.FareRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetFare(
    private val fareRepository: FareRepository
) {
    suspend fun awaitSingle(
        originStationId: String,
        destinationStationId: String,
    ): Fare? {
        return withContext(Dispatchers.IO) {
            fareRepository.await(originStationId, destinationStationId)
        }
    }
}