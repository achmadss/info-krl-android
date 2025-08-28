package dev.achmad.data.remote.model.station

import dev.achmad.data.local.entity.station.StationUpdate

fun StationResponse.toStationUpdate(): StationUpdate {
    return StationUpdate(
        uid = uid,
        id = id,
        name = name,
        type = type,
        active = metadata.active,
        daop = metadata.origin?.daop,
        fgEnable = metadata.origin?.fgEnable,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
