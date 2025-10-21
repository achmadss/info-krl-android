package dev.achmad.domain.usecase.route

import dev.achmad.domain.repository.RouteRepository

class SyncRoute(
    private val routeRepository: RouteRepository
) {
    suspend fun await(trainId: String): Result {
        return try {
            val route = routeRepository.fetch(trainId)
            routeRepository.store(route)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    sealed interface Result {
        data object Success : Result
        data class Error(val error: Throwable) : Result
    }

}
