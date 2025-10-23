package dev.achmad.domain.route.interactor

import dev.achmad.domain.route.repository.RouteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WipeRouteTables(
    private val routeRepository: RouteRepository
) {

    suspend fun await() {
        withContext(Dispatchers.IO) {
            routeRepository.deleteAll()
        }
    }

}
