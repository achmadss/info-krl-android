package dev.achmad.domain.repository

import dev.achmad.domain.model.Route
import kotlinx.coroutines.flow.Flow

interface RouteRepository {

    val routes: Flow<List<Route>>

    suspend fun fetchAndStoreByTrainId(trainId: String)
    fun subscribeSingle(trainId: String): Flow<Route?>

}