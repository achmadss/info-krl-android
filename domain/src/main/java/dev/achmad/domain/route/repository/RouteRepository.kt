package dev.achmad.domain.route.repository

import dev.achmad.domain.route.model.Route
import kotlinx.coroutines.flow.Flow

interface RouteRepository {

    fun subscribeAll(): Flow<List<Route>>
    fun subscribe(trainId: String): Flow<Route?>

    suspend fun awaitAll(trainId: String): List<Route>
    suspend fun fetch(trainId: String): Route
    suspend fun store(route: Route)

}