package dev.achmad.data.remote.model.fare

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FareResponse(
    @SerialName("id") val id: String,
    @SerialName("station_from") val stationFrom: String,
    @SerialName("station_to") val stationTo: String,
    @SerialName("station_name_from") val stationNameFrom: String,
    @SerialName("station_name_to") val stationNameTo: String,
    @SerialName("fare") val fare: String,
    @SerialName("distance") val distance: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)
