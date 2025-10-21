package dev.achmad.data.remote.model.station

import dev.achmad.core.util.toLocalDateTime
import dev.achmad.domain.model.Station

fun StationResponse.toDomain(): Station {
    return Station(
        id = id,
        uid = uid,
        name = name,
        type = Station.Type(type),
        active = metadata?.active ?: false,
        daop = metadata?.origin?.daop,
        fgEnable = metadata?.origin?.fgEnable,
        favorite = false,
        favoritePosition = null,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime()
    )
}
