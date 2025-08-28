package dev.achmad.domain.model

import java.time.LocalDateTime

data class Route(
    val details: Details,
    val routes: List<Routes>,
) {
    data class Details(
        val line: String,
        val route: String,
        val stationDestinationId: String,
        val stationDestinationName: String,
        val stationOriginId: String,
        val stationOriginName: String,
        val trainId: String,
        val arrivesAt: LocalDateTime,
    )
    data class Routes(
        val id: String,
        val stationId: String,
        val stationName: String,
        val departsAt: LocalDateTime,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
    )
}
