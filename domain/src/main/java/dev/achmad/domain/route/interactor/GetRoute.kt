package dev.achmad.domain.route.interactor

import dev.achmad.domain.route.model.Route
import dev.achmad.domain.route.repository.RouteRepository
import kotlinx.coroutines.flow.Flow

class GetRoute(
    private val routeRepository: RouteRepository
) {

    fun subscribe(trainId: String): Flow<Route?> {
        return routeRepository.subscribe(trainId)
    }

}
