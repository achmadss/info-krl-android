package dev.achmad.data.local.entity.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import dev.achmad.data.local.entity.route.RouteEntity.Stops
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Entity(tableName = "routes")
@TypeConverters(RouteEntityConverters::class)
data class RouteEntity(
    @PrimaryKey
    @ColumnInfo(name = "train_id") val trainId: String,
    @ColumnInfo(name = "line") val line: String,
    @ColumnInfo(name = "route") val route: String,
    @ColumnInfo(name = "station_origin_id") val stationOriginId: String,
    @ColumnInfo(name = "station_origin_name") val stationOriginName: String,
    @ColumnInfo(name = "station_destination_id") val stationDestinationId: String,
    @ColumnInfo(name = "station_destination_name") val stationDestinationName: String,
    @ColumnInfo(name = "arrives_at") val arrivesAt: String,
    val stops: List<Stops>,
) {
    @Serializable
    data class Stops(
        @ColumnInfo(name = "id") val id: String,
        @ColumnInfo(name = "station_id") val stationId: String,
        @ColumnInfo(name = "station_name") val stationName: String,
        @ColumnInfo(name = "departs_at") val departsAt: String,
        @ColumnInfo(name = "created_at") val createdAt: String,
        @ColumnInfo(name = "updated_at") val updatedAt: String
    )
}

class RouteEntityConverters {
    @TypeConverter
    fun fromRouteItems(list: List<Stops>): String =
        Json.encodeToString(list)

    @TypeConverter
    fun toRouteItems(json: String): List<Stops> =
        Json.decodeFromString(json)
}