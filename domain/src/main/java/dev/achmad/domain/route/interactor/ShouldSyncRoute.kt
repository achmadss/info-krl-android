package dev.achmad.domain.route.interactor

import dev.achmad.domain.route.repository.RouteRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ShouldSyncRoute(
    private val routeRepository: RouteRepository
) {
    suspend fun await(trainId: String): Boolean {
        val routes = routeRepository.awaitAll(trainId)
        if (routes.isEmpty()) {
            return true
        }

        val lastUpdated = routes
            .maxByOrNull { it.arrivesAt }
            ?.arrivesAt
            ?: return true

        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.ofInstant(Instant.now(), zone)
        return now.toLocalDate().isAfter(lastUpdated.toLocalDate())
    }
}