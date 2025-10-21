package dev.achmad.data.remote.model.route

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteResponse(
    @SerialName("routes") val routes: List<StopResponse>,
    @SerialName("details") val details: DetailsResponse
) {
    @Serializable
    data class StopResponse(
        @SerialName("station_id") val stationId: String,
        @SerialName("station_name") val stationName: String,
        @SerialName("departs_at") val departsAt: String,
        @SerialName("created_at") val createdAt: String,
        @SerialName("updated_at") val updatedAt: String
    )

    @Serializable
    data class DetailsResponse(
        @SerialName("train_id") val trainId: String,
        @SerialName("line") val line: String,
        @SerialName("route") val route: String,
        @SerialName("station_origin_id") val stationOriginId: String,
        @SerialName("station_origin_name") val stationOriginName: String,
        @SerialName("station_destination_id") val stationDestinationId: String,
        @SerialName("station_destination_name") val stationDestinationName: String,
        @SerialName("arrives_at") val arrivesAt: String
    )
}
