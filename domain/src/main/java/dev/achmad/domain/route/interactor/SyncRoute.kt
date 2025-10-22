package dev.achmad.domain.route.interactor

import dev.achmad.domain.route.repository.RouteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    sealed interface Result {
        data object Success : Result
        data class Error(val error: Throwable) : Result
    }

}
