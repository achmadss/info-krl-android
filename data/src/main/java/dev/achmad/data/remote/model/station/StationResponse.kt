package dev.achmad.data.remote.model.station

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StationResponse(
    @SerialName("id") val id: String,
    @SerialName("uid") val uid: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("metadata") val metadata: Metadata,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
) {
    @Serializable
    data class Metadata(
        @SerialName("active") val active: Boolean? = null,
        @SerialName("origin") val origin: Origin? = null,
    ) {
        @Serializable
        data class Origin(
            @SerialName("daop") val daop: Int? = null,
            @SerialName("fg_enable") val fgEnable: Int? = null,
        )
    }
}