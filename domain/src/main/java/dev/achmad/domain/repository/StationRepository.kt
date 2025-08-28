package dev.achmad.domain.repository

import dev.achmad.domain.model.Station
import kotlinx.coroutines.flow.Flow

interface StationRepository {

    val stations: Flow<List<Station>>
    val favoriteStations: Flow<List<Station>>

    suspend fun refresh()
    suspend fun toggleFavorite(station: Station, favorite: Boolean)
    fun subscribeSingle(id: String): Flow<Station?>

}