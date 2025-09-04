package dev.achmad.domain.repository

import dev.achmad.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    val schedules: Flow<List<Schedule>>

    suspend fun fetchByStationId(stationId: String)
    suspend fun subscribeByStationId(
        stationId: String,
        skipPastSchedule: Boolean = false
    ): Flow<List<Schedule>>

}