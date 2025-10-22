package dev.achmad.domain.station.repository

import dev.achmad.domain.station.model.Station
import kotlinx.coroutines.flow.Flow

interface StationRepository {

    fun subscribeAll(favorite: Boolean? = null): Flow<List<Station>>
    fun subscribeSingle(id: String): Flow<Station?>
    fun subscribeMultiple(ids: List<String>): Flow<List<Station>>

    suspend fun awaitAll(favorite: Boolean? = null): List<Station>
    suspend fun awaitSingle(id: String): Station?

    suspend fun fetch(): List<Station>
    suspend fun store(stations: List<Station>)

    suspend fun favorite(stationId: String)
    suspend fun unfavorite(stationId: String)
    suspend fun update(stations: List<Station>)

}