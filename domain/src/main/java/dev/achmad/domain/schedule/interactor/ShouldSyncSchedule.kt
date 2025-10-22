package dev.achmad.domain.schedule.interactor

import dev.achmad.domain.schedule.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ShouldSyncSchedule(
    private val scheduleRepository: ScheduleRepository
) {
    suspend fun await(stationId: String): Boolean {
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
}