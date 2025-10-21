package dev.achmad.domain.schedule.interactor

import dev.achmad.domain.schedule.repository.ScheduleRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ShouldSyncSchedule(
    private val scheduleRepository: ScheduleRepository
) {
    suspend fun await(stationId: String): Boolean {
        val schedules = scheduleRepository.awaitAll(stationId)
        if (schedules.isEmpty()) {
            return true
        }

        val lastUpdated = schedules
            .maxByOrNull { it.updatedAt }
            ?.updatedAt
            ?: return true

        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.ofInstant(Instant.now(), zone)
        return now.toLocalDate().isAfter(lastUpdated.toLocalDate())
    }
}