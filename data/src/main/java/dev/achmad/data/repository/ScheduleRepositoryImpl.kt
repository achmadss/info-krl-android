package dev.achmad.data.repository

import dev.achmad.core.network.parseAs
import dev.achmad.data.local.InfoKRLDatabase
import dev.achmad.data.local.dao.ScheduleDao
import dev.achmad.data.local.entity.schedule.toDomain
import dev.achmad.data.remote.InfoKRLApi
import dev.achmad.data.remote.model.BaseResponse
import dev.achmad.data.remote.model.schedule.ScheduleResponse
import dev.achmad.data.remote.model.schedule.toEntity
import dev.achmad.domain.model.Schedule
import dev.achmad.domain.repository.ScheduleRepository
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

    override val schedules: Flow<List<Schedule>> =
        scheduleDao.subscribeAll()
            .map { it.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    override suspend fun fetchAndStoreByStationId(stationId: String) {
        withContext(Dispatchers.IO) {
            val response = api.getScheduleByStationId(stationId)
            val data = response.parseAs<BaseResponse<List<ScheduleResponse>>>()
            if (data.metadata.success == false) {
                throw Exception(data.metadata.message)
            }
            val schedules = data.data.map { it.toEntity() }
            if (schedules.isEmpty()) {
                throw Exception("data is empty")
            }
            scheduleDao.insert(schedules)
        }
    }


    override fun subscribeSingle(stationId: String): Flow<List<Schedule>> {
        return scheduleDao.subscribeAllByStationId(stationId)
            .mapNotNull { it.toDomain().filter { !it.trainId.contains("/") } }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

}