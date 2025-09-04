package dev.achmad.data.repository

import androidx.room.withTransaction
import dev.achmad.data.local.ComulineDatabase
import dev.achmad.data.local.dao.ScheduleDao
import dev.achmad.data.local.dao.StationDao
import dev.achmad.data.local.entity.schedule.toDomain
import dev.achmad.data.remote.ComulineApi
import dev.achmad.data.remote.model.schedule.toEntity
import dev.achmad.domain.model.Schedule
import dev.achmad.domain.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class ScheduleRepositoryImpl(
    private val api: ComulineApi,
    private val database: ComulineDatabase,
    private val stationDao: StationDao = database.stationDao(),
    private val scheduleDao: ScheduleDao = database.scheduleDao(),
): ScheduleRepository {

    override val schedules: Flow<List<Schedule>> =
        scheduleDao.subscribeAll()
            .map { it.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    override suspend fun fetchByStationId(stationId: String) {
        withContext(Dispatchers.IO) {
            val schedules = api.getScheduleByStationId(stationId).data.map { it.toEntity() }
            database.withTransaction {
                scheduleDao.insert(schedules)
                val station = stationDao.awaitSingle(stationId)
                    ?: throw NullPointerException("Cannot find station with id $stationId")
                stationDao.update(
                    station.copy(hasFetchedSchedulePreviously = true)
                )
            }
        }
    }

    override suspend fun subscribeByStationId(
        stationId: String,
        skipPastSchedule: Boolean,
    ): Flow<List<Schedule>> {
        val now = LocalDateTime.now()
        return scheduleDao.subscribeAllByStationId(stationId)
            .map {
                val schedules = it.toDomain()
                if (skipPastSchedule) {
                    schedules.filter { it.departsAt.isAfter(now) }
                } else schedules
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

}