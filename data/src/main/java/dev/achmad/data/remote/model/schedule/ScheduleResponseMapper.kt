package dev.achmad.data.remote.model.schedule

import dev.achmad.data.local.entity.schedule.ScheduleEntity

fun ScheduleResponse.toEntity(): ScheduleEntity {
    return ScheduleEntity(
        id = id,
        stationId = stationId,
        stationOriginId = stationOriginId,
        stationDestinationId = stationDestinationId,
        trainId = trainId,
        line = line,
        route = route,
        departsAt = departsAt,
        arrivesAt = arrivesAt,
        color = metadata?.origin?.color,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}