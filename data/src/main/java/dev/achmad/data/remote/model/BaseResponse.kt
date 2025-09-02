package dev.achmad.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    @SerialName("metadata") val metadata: Metadata,
    @SerialName("data") val data: T
) {
    @Serializable
    data class Metadata(
        @SerialName("success") val success: Boolean? = null,
        @SerialName("message") val message: String? = null,
    )
}
