package dev.achmad.domain.repository

import dev.achmad.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    fun subscribeAll(): Flow<List<Schedule>>
    fun subscribe(stationId: String): Flow<List<Schedule>>

    suspend fun fetch(stationId: String): List<Schedule>
    suspend fun store(schedules: List<Schedule>)

}