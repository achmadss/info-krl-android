package dev.achmad.domain.station.model

import dev.achmad.core.util.enumValueOfOrDefault
import java.time.LocalDateTime

data class Station(
    val id: String,
    val uid: String,
    val name: String,
    val favorite: Boolean,
    val favoritePosition: Int?,
    val type: Type,
    val active: Boolean?,
    val daop: Int?,
    val fgEnable: Int?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    enum class Type {
        KRL, LOCAL;
        companion object {
            operator fun invoke(value: String?) = enumValueOfOrDefault(value, KRL)
        }
    }
}
