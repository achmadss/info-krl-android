package dev.achmad.domain.usecase.schedule

import dev.achmad.domain.repository.ScheduleRepository

class SyncSchedule(
    private val scheduleRepository: ScheduleRepository
) {

    sealed interface Result {
        data object Success : Result
        data class Error(val error: Throwable) : Result
    }

    suspend fun await(stationId: String): Result {
        return try {
            val schedules = scheduleRepository.fetch(stationId)
            scheduleRepository.store(schedules)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

}
