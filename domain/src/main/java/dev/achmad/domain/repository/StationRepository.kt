package dev.achmad.domain.repository

import dev.achmad.domain.model.Station
import kotlinx.coroutines.flow.Flow

interface StationRepository {

    fun subscribeAll(favorite: Boolean? = null): Flow<List<Station>>
    fun subscribeSingle(id: String): Flow<Station?>

    suspend fun awaitAllFavorites(): List<Station>
    suspend fun awaitSingle(id: String): Station?

    suspend fun fetchAndStore()
    suspend fun toggleFavorite(station: Station)
    suspend fun updateFavorite(station: Station)

    suspend fun reorderFavorites(stations: List<Station>)

}