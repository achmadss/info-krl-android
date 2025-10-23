package dev.achmad.domain.schedule.interactor

import dev.achmad.domain.schedule.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WipeScheduleTables(
    private val scheduleRepository: ScheduleRepository,
) {

    suspend fun await() {
        withContext(Dispatchers.IO) {
            scheduleRepository.deleteAll()
        }
    }

}