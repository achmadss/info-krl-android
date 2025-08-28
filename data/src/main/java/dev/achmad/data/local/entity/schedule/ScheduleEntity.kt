package dev.achmad.data.local.entity.schedule

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "station_id")
    val stationId: String,

    @ColumnInfo(name = "station_origin_id")
    val stationOriginId: String,

    @ColumnInfo(name = "station_destination_id")
    val stationDestinationId: String,

    @ColumnInfo(name = "train_id")
    val trainId: String,

    @ColumnInfo(name = "line")
    val line: String,

    @ColumnInfo(name = "route")
    val route: String,

    @ColumnInfo(name = "departs_at")
    val departsAt: String,

    @ColumnInfo(name = "arrives_at")
    val arrivesAt: String,

    @ColumnInfo(name = "color")
    val color: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)