package dev.achmad.domain.model

import java.time.LocalDateTime

data class Route(
    val trainId: String,
    val line: String,
    val route: String,
    val stationOriginId: String,
    val stationOriginName: String,
    val stationDestinationId: String,
    val stationDestinationName: String,
    val arrivesAt: LocalDateTime,
    val stops: List<Stops>
) {
    data class Stops(
        val stationId: String,
        val stationName: String,
        val departsAt: LocalDateTime,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )
}