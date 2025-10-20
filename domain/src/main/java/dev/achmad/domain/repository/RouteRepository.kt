package dev.achmad.domain.repository

import dev.achmad.domain.model.Route
import kotlinx.coroutines.flow.Flow

interface RouteRepository {

    fun subscribeAll(): Flow<List<Route>>
    fun subscribeSingle(trainId: String): Flow<Route?>
    suspend fun fetchAndStoreByTrainId(trainId: String)

}