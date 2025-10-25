package dev.achmad.domain.fare.repository

import dev.achmad.domain.fare.model.Fare
import kotlinx.coroutines.flow.Flow

interface FareRepository {

    suspend fun await(
        originStationId: String,
        destinationStationId: String,
    ): Fare?

    fun subscribe(
        originStationId: String,
        destinationStationId: String,
    ): Flow<Fare?>

    suspend fun fetch(
        originStationId: String,
        destinationStationId: String,
    ): Fare

    suspend fun store(fare: Fare)

    suspend fun deleteAll()

}