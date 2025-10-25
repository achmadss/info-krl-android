package dev.achmad.domain.route.interactor

import dev.achmad.domain.route.repository.RouteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SyncRoute(
    private val routeRepository: RouteRepository
) {
    suspend fun await(
        trainId: String,
        checkShouldSync: Boolean = true
    ): Result {
        return withContext(Dispatchers.IO) {
            if (checkShouldSync && !shouldSync(trainId)) Result.AlreadySynced
            try {
                val route = routeRepository.fetch(trainId)
                routeRepository.store(route)
                Result.Success
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    fun subscribe(
        trainId: String,
        checkShouldSync: Boolean = true
    ): Flow<Result> = flow {
        emit(Result.Loading)
        emit(await(trainId, checkShouldSync))
    }

    private suspend fun shouldSync(trainId: String): Boolean {
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
        data object Loading : Result
        data object AlreadySynced : Result
        data class Error(val error: Throwable) : Result
    }

}
