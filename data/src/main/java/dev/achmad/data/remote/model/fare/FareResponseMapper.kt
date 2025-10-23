package dev.achmad.data.remote.model.fare

import dev.achmad.data.local.entity.fare.FareEntity

fun FareResponse.toEntity(): FareEntity {
    return FareEntity(
        id = id,
        stationFrom = stationFrom,
        stationTo = stationTo,
        stationNameFrom = stationNameFrom,
        stationNameTo = stationNameTo,
        fare = fare.toDoubleOrNull() ?: 0.0,
        distance = distance.toDoubleOrNull() ?: 0.0,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}