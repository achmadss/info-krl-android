package dev.achmad.data.local.entity.schedule

import dev.achmad.core.util.format
import dev.achmad.core.util.toLocalDateTime
import dev.achmad.domain.model.Schedule

fun ScheduleEntity.toDomain(): Schedule {
    return Schedule(
        id = id,
        stationId = stationId,
        stationOriginId = stationOriginId,
        stationDestinationId = stationDestinationId,
        trainId = trainId,
        line = line,
        route = route,
        departsAt = departsAt.toLocalDateTime(),
        arrivesAt = arrivesAt.toLocalDateTime(),
        color = color,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime(),
    )
}

fun Schedule.toEntity(): ScheduleEntity {
    return ScheduleEntity(
        id = id,
        stationId = stationId,
        stationOriginId = stationOriginId,
        stationDestinationId = stationDestinationId,
        trainId = trainId,
        line = line,
        route = route,
        departsAt = departsAt.format(),
        arrivesAt = arrivesAt.format(),
        color = color,
        createdAt = createdAt.format(),
        updatedAt = updatedAt.format(),
    )
}

fun List<ScheduleEntity>.toDomain(): List<Schedule> {
    return map { it.toDomain() }
}

fun List<Schedule>.toEntity(): List<ScheduleEntity> {
    return map { it.toEntity() }
}