package dev.achmad.data.local.entity.schedule

import dev.achmad.core.util.toLocalDateTime
import dev.achmad.core.util.toUtcString
import dev.achmad.domain.schedule.model.Schedule

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
        departsAt = departsAt.toUtcString(),
        arrivesAt = arrivesAt.toUtcString(),
        color = color,
        createdAt = createdAt.toUtcString(),
        updatedAt = updatedAt.toUtcString(),
    )
}

fun List<ScheduleEntity>.toDomain(): List<Schedule> {
    return map { it.toDomain() }
}

fun List<Schedule>.toEntity(): List<ScheduleEntity> {
    return map { it.toEntity() }
}