package dev.achmad.domain.schedule.interactor

import dev.achmad.domain.schedule.repository.ScheduleRepository

class WipeScheduleTables(
    private val scheduleRepository: ScheduleRepository,
) {

    suspend fun execute() {
        scheduleRepository.deleteAll()
    }

}