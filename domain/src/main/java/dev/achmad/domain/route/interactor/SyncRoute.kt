package dev.achmad.domain.route.interactor

import dev.achmad.domain.route.repository.RouteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SyncRoute(
    private val routeRepository: RouteRepository
) {
    suspend fun await(trainId: String): Result {
        return withContext(Dispatchers.IO) {
            try {
                val route = routeRepository.fetch(trainId)
                routeRepository.store(route)
                Result.Success
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    suspend fun shouldSync(trainId: String): Boolean {
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

    sealed interface Result {
        data object Success : Result
        data class Error(val error: Throwable) : Result
    }

}
