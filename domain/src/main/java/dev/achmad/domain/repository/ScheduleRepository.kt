package dev.achmad.domain.repository

import dev.achmad.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    val schedules: Flow<List<Schedule>>

    suspend fun refreshScheduleByStationId(stationId: String)
    suspend fun subscribeByStationId(
        stationId: String,
        skipPastSchedule: Boolean = true
    ): Flow<List<Schedule>>

}