package dev.achmad.domain.schedule.interactor

import dev.achmad.domain.schedule.model.Schedule
import dev.achmad.domain.schedule.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow

class GetSchedule(
    private val scheduleRepository: ScheduleRepository
) {

    fun subscribe(stationId: String): Flow<List<Schedule>> {
        return scheduleRepository.subscribe(stationId)
    }

}
