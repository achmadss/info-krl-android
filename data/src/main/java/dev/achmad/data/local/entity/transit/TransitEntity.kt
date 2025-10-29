package dev.achmad.data.local.entity.transit

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Entity(tableName = "transits",)
@TypeConverters(TransitEntityConverters::class)
data class TransitEntity(
    @PrimaryKey
    @ColumnInfo("transit_id") val transitId: String,
    @ColumnInfo("origin_station_id") val originStationId: String,
    @ColumnInfo("origin_station_name") val originStationName: String,
    @ColumnInfo("destination_station_id") val destinationStationId: String,
    @ColumnInfo("destination_station_name") val destinationStationName: String,
    @ColumnInfo("total_stops") val totalStops: String,
    @ColumnInfo("routes") val routeGroups: List<Routes>
) {
    @Serializable
    data class Routes(
        @ColumnInfo("line") val line: String,
        @ColumnInfo("stations") val stations: List<Stations>
    ) {
        @Serializable
        data class Stations(
            @ColumnInfo("station_id") val stationId: String,
            @ColumnInfo("station_name") val stationName: String
        )
    }
}

class TransitEntityConverters {
    @TypeConverter
    fun fromRouteItems(list: List<TransitEntity.Routes>): String =
        Json.encodeToString(list)

    @TypeConverter
    fun toRouteItems(json: String): List<TransitEntity.Routes> =
        Json.decodeFromString(json)

    @TypeConverter
    fun fromStationItems(list: List<TransitEntity.Routes.Stations>): String =
        Json.encodeToString(list)

    @TypeConverter
    fun toStationItems(json: String): List<TransitEntity.Routes.Stations> =
        Json.decodeFromString(json)
}