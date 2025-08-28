package dev.achmad.data.repository

import dev.achmad.data.local.dao.ScheduleDao
import dev.achmad.data.local.entity.schedule.toDomain
import dev.achmad.data.remote.ComulineApi
import dev.achmad.data.remote.model.schedule.toEntity
import dev.achmad.domain.model.Schedule
import dev.achmad.domain.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ScheduleRepositoryImpl(
    private val scheduleDao: ScheduleDao,
    private val api: ComulineApi,
): ScheduleRepository {

    override val schedules: Flow<List<Schedule>> =
        scheduleDao.subscribeAll().map { it.toDomain() }

    override suspend fun refreshScheduleByStationId(stationId: String) {
        withContext(Dispatchers.IO) {
            val schedules = api.getScheduleByStationId(stationId).data.map { it.toEntity() }
            scheduleDao.insert(schedules)
        }
    }

}