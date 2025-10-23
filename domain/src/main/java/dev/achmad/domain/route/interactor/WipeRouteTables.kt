package dev.achmad.domain.route.interactor

import dev.achmad.domain.route.repository.RouteRepository

class WipeRouteTables(
    private val routeRepository: RouteRepository
) {

    suspend fun execute() {
        routeRepository.deleteAll()
    }

}
