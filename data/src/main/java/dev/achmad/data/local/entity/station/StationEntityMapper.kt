package dev.achmad.data.local.entity.station

import dev.achmad.core.util.format
import dev.achmad.core.util.toLocalDateTime
import dev.achmad.domain.model.Station

fun StationEntity.toDomain(): Station {
    return Station(
        uid = uid,
        id = id,
        name = name,
        favorite = favorite ?: false,
        favoritePosition = favoritePosition,
        type = Station.Type(type),
        active = active,
        daop = daop,
        fgEnable = fgEnable,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime(),
    )
}

fun Station.toEntity(): StationEntity {
    return StationEntity(
        uid = uid,
        id = id,
        name = name,
        favorite = favorite,
        favoritePosition = favoritePosition,
        type = type.name,
        active = active,
        daop = daop,
        fgEnable = fgEnable,
        createdAt = createdAt.format(),
        updatedAt = updatedAt.format(),
    )
}

fun List<StationEntity>.toDomain(): List<Station> {
    return map { it.toDomain() }
}

fun List<Station>.toEntity(): List<StationEntity> {
    return map { it.toEntity() }
}