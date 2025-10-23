package dev.achmad.domain.fare.repository

import dev.achmad.domain.fare.model.Fare

interface FareRepository {

    suspend fun await(
        originStationId: String,
        destinationStationId: String,
    ): Fare?

    suspend fun fetch(
        originStationId: String,
        destinationStationId: String,
    ): Fare

    suspend fun store(fare: Fare)

    suspend fun deleteAll()

}