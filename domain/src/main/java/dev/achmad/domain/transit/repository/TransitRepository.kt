package dev.achmad.domain.transit.repository

import dev.achmad.domain.transit.model.Transit
import kotlinx.coroutines.flow.Flow

interface TransitRepository {

    fun subscribeAll(): Flow<List<Transit>>
    fun subscribe(
        originStationId: String,
        destinationStationId: String
    ): Flow<Transit?>

    suspend fun await(
        originStationId: String,
        destinationStationId: String
    ): Transit?
    suspend fun awaitAll(): List<Transit>
    suspend fun fetch(
        originStationId: String,
        destinationStationId: String
    ): Transit
    suspend fun store(transit: Transit)

    suspend fun deleteAll()

}