package dev.achmad.data.local.entity.fare

import dev.achmad.core.util.toLocalDateTime
import dev.achmad.core.util.toUtcString
import dev.achmad.domain.fare.model.Fare

fun FareEntity.toDomain(): Fare {
    return Fare(
        id = id,
        stationFrom = stationFrom,
        stationTo = stationTo,
        stationNameFrom = stationNameFrom,
        stationNameTo = stationNameTo,
        fare = fare,
        distance = distance,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime()
    )
}

fun Fare.toEntity(): FareEntity {
    return FareEntity(
        id = id,
        stationFrom = stationFrom,
        stationTo = stationTo,
        stationNameFrom = stationNameFrom,
        stationNameTo = stationNameTo,
        fare = fare,
        distance = distance,
        createdAt = createdAt.toUtcString(),
        updatedAt = updatedAt.toUtcString()
    )
}