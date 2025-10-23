package dev.achmad.data.local.entity.fare

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fares")
data class FareEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "station_from") val stationFrom: String,
    @ColumnInfo(name = "station_to") val stationTo: String,
    @ColumnInfo(name = "station_name_from") val stationNameFrom: String,
    @ColumnInfo(name = "station_name_to") val stationNameTo: String,
    @ColumnInfo(name = "fare") val fare: Double,
    @ColumnInfo(name = "distance") val distance: Double,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
)