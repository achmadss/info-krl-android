package dev.achmad.data.remote.model.transit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransitResponse(
    @SerialName("origin_station_id") val originStationId: String,
    @SerialName("origin_station_name") val originStationName: String,
    @SerialName("destination_station_id") val destinationStationId: String,
    @SerialName("destination_station_name") val destinationStationName: String,
    @SerialName("total_stops") val totalStops: Int,
    @SerialName("routes") val routeGroups: List<RouteResponse>
) {
    @Serializable
    data class RouteResponse(
        @SerialName("line") val line: String,
        @SerialName("stations") val stations: List<StationResponse>
    ) {
        @Serializable
        data class StationResponse(
            @SerialName("station_id") val stationId: String,
            @SerialName("station_name") val stationName: String
        )
    }
}