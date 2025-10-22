package dev.achmad.domain.schedule.interactor

import dev.achmad.domain.schedule.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncSchedule(
    private val scheduleRepository: ScheduleRepository
) {
    suspend fun await(stationId: String): Result {
        return withContext(Dispatchers.IO) {
            try {
                val schedules = scheduleRepository.fetch(stationId)
                scheduleRepository.store(schedules)
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
