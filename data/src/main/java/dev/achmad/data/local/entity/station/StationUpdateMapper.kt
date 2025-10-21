package dev.achmad.data.local.entity.station

import dev.achmad.core.util.format
import dev.achmad.domain.model.Station

fun Station.toStationUpdate(): StationUpdate {
    return StationUpdate(
        uid = uid,
        id = id,
        name = name,
        type = type.name,
        favorite = favorite,
        favoritePosition = favoritePosition,
        active = active,
        daop = daop,
        fgEnable = fgEnable,
        createdAt = createdAt.format(),
        updatedAt = updatedAt.format()
    )
}