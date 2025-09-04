package dev.achmad.domain.repository

import dev.achmad.domain.model.Station
import kotlinx.coroutines.flow.Flow

interface StationRepository {

    val stations: Flow<List<Station>>
    val favoriteStations: Flow<List<Station>>

    suspend fun refresh()
    suspend fun toggleFavorite(station: Station)
    suspend fun awaitSingle(id: String): Station?

}