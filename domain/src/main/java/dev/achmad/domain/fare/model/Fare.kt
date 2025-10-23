package dev.achmad.domain.fare.model

import java.time.LocalDateTime

data class Fare(
    val id: String,
    val stationFrom: String,
    val stationTo: String,
    val stationNameFrom: String,
    val stationNameTo: String,
    val fare: Double,
    val distance: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
