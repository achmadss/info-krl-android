package dev.achmad.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.achmad.data.local.dao.ScheduleDao
import dev.achmad.data.local.dao.StationDao
import dev.achmad.data.local.entity.schedule.ScheduleEntity
import dev.achmad.data.local.entity.station.StationEntity

@Database(
    entities = [
        ScheduleEntity::class,
        StationEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class ComulineDatabase: RoomDatabase() {
    abstract fun stationDao(): StationDao
    abstract fun scheduleDao(): ScheduleDao
}