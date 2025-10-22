package dev.achmad.domain.route.interactor

import dev.achmad.domain.route.repository.RouteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ShouldSyncRoute(
    private val routeRepository: RouteRepository
) {
    suspend fun await(trainId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val routes = routeRepository.awaitAll(trainId)
            if (routes.isEmpty()) {
                return@withContext true
            }

            val lastUpdated = routes
                .maxByOrNull { it.arrivesAt }
                ?.arrivesAt
                ?: return@withContext true

            val zone = ZoneId.systemDefault()
            val now = LocalDateTime.ofInstant(Instant.now(), zone)
            return@withContext now.toLocalDate().isAfter(lastUpdated.toLocalDate())
        }
    }
}