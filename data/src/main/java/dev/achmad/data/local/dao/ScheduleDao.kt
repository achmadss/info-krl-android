package dev.achmad.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.achmad.data.local.entity.schedule.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("select * from schedules where id = :id")
    suspend fun awaitSingle(id: String): ScheduleEntity?

    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    fun subscribeSingle(id: String): Flow<ScheduleEntity?>

    @Query("SELECT * FROM schedules WHERE id IN (:ids)")
    suspend fun awaitMultiple(ids: List<String>): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE id IN (:ids)")
    fun subscribeMultiple(ids: List<String>): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules")
    suspend fun awaitAll(): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE station_id = :stationId")
    suspend fun awaitAllByStationId(stationId: String): List<ScheduleEntity>

    @Query("SELECT * FROM schedules")
    fun subscribeAll(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE station_id = :stationId")
    fun subscribeAllByStationId(stationId: String): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: ScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg schedules: ScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedules: List<ScheduleEntity>)

    @Delete
    suspend fun delete(schedule: ScheduleEntity)

    @Delete
    suspend fun delete(vararg schedules: ScheduleEntity)

    @Delete
    suspend fun delete(schedules: List<ScheduleEntity>)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()

}