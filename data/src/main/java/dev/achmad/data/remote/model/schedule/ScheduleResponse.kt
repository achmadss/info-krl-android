package dev.achmad.data.remote.model.schedule

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleResponse(
    @SerialName("id") val id: String,
    @SerialName("line") val line: String,
    @SerialName("route") val route: String,
    @SerialName("station_destination_id") val stationDestinationId: String,
    @SerialName("station_id") val stationId: String,
    @SerialName("station_origin_id") val stationOriginId: String,
    @SerialName("train_id") val trainId: String,
    @SerialName("departs_at") val departsAt: String,
    @SerialName("arrives_at") val arrivesAt: String,
    @SerialName("metadata") val metadata: Metadata? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
) {
    @Serializable
    data class Metadata(
        @SerialName("origin") val origin: Origin? = null
    ) {
        @Serializable
        data class Origin(
            @SerialName("color") val color: String? = null
        )
    }
}
