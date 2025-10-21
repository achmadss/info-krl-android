package dev.achmad.data.repository

import dev.achmad.core.network.parseAs
import dev.achmad.data.local.InfoKRLDatabase
import dev.achmad.data.local.dao.ScheduleDao
import dev.achmad.data.local.entity.schedule.toDomain
import dev.achmad.data.local.entity.schedule.toEntity as domainToEntity
import dev.achmad.data.remote.InfoKRLApi
import dev.achmad.data.remote.model.BaseResponse
import dev.achmad.data.remote.model.schedule.ScheduleResponse
import dev.achmad.data.remote.model.schedule.toEntity as responseToEntity
import dev.achmad.domain.schedule.model.Schedule
import dev.achmad.domain.schedule.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext

class ScheduleRepositoryImpl(
    private val api: InfoKRLApi,
    private val database: InfoKRLDatabase,
): ScheduleRepository {

    private val scheduleDao: ScheduleDao = database.scheduleDao()

    override fun subscribeAll(): Flow<List<Schedule>> =
        scheduleDao.subscribeAll()
            .map { it.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    override fun subscribe(stationId: String): Flow<List<Schedule>> {
        return scheduleDao.subscribeAllByStationId(stationId)
            .mapNotNull { it.toDomain().filter { !it.trainId.contains("/") } }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override suspend fun fetch(stationId: String): List<Schedule> {
        return withContext(Dispatchers.IO) {
            val response = api.getScheduleByStationId(stationId)
            val data = response.parseAs<BaseResponse<List<ScheduleResponse>>>()
            if (data.metadata.success == false) {
                throw Exception(data.metadata.message)
            }
            if (data.data.isEmpty()) {
                throw Exception("data is empty")
            }
            data.data.map { it.responseToEntity().toDomain() }
                .filter { !it.trainId.contains("/") }
        }
    }

    override suspend fun awaitAll(stationId: String): List<Schedule> {
        return withContext(Dispatchers.IO) {
            scheduleDao.awaitAllByStationId(stationId).toDomain()
        }
    }

    override suspend fun store(schedules: List<Schedule>) {
        withContext(Dispatchers.IO) {
            val scheduleEntities = schedules.map { it.domainToEntity() }
            scheduleDao.insert(scheduleEntities)
        }
    }

}