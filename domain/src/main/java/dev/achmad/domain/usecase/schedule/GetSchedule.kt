package dev.achmad.domain.usecase.schedule

import dev.achmad.domain.model.Schedule
import dev.achmad.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow

class GetSchedule(
    private val scheduleRepository: ScheduleRepository
) {

    fun subscribe(): Flow<List<Schedule>> {
        return scheduleRepository.subscribeAll()
    }

    fun subscribe(stationId: String): Flow<List<Schedule>> {
        return scheduleRepository.subscribe(stationId)
    }

}
