package dev.achmad.domain.usecase.route

import dev.achmad.domain.model.Route
import dev.achmad.domain.repository.RouteRepository
import kotlinx.coroutines.flow.Flow

class GetRoute(
    private val routeRepository: RouteRepository
) {

    fun subscribe(): Flow<List<Route>> {
        return routeRepository.subscribeAll()
    }

    fun subscribe(trainId: String): Flow<Route?> {
        return routeRepository.subscribe(trainId)
    }

}
