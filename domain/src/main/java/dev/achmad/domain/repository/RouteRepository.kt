package dev.achmad.domain.repository

import dev.achmad.domain.model.Route
import kotlinx.coroutines.flow.Flow

interface RouteRepository {

    fun subscribeAll(): Flow<List<Route>>
    fun subscribe(trainId: String): Flow<Route?>

    suspend fun fetch(trainId: String): Route
    suspend fun store(route: Route)

}