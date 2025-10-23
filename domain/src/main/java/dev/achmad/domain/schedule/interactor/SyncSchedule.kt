package dev.achmad.domain.schedule.interactor

import dev.achmad.domain.schedule.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

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

    suspend fun shouldSync(stationId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val schedules = scheduleRepository.awaitAll(stationId)
            if (schedules.isEmpty()) {
                return@withContext true
            }

            val lastUpdated = schedules
                .maxByOrNull { it.updatedAt }
                ?.updatedAt
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
