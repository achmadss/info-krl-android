package dev.achmad.domain.repository

import dev.achmad.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    fun subscribeAll(): Flow<List<Schedule>>
    fun subscribeSingle(stationId: String): Flow<List<Schedule>>
    suspend fun fetchAndStoreByStationId(stationId: String)

}