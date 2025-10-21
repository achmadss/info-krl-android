package dev.achmad.domain.schedule.model

import java.time.LocalDateTime

data class Schedule(
    val id: String,
    val line: String,
    val route: String,
    val stationDestinationId: String,
    val stationId: String,
    val stationOriginId: String,
    val trainId: String,
    val departsAt: LocalDateTime,
    val arrivesAt: LocalDateTime,
    val color: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
