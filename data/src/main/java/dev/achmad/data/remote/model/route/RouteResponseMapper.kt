package dev.achmad.data.remote.model.route

import dev.achmad.data.local.entity.route.RouteEntity

fun RouteResponse.toEntity(): RouteEntity {
    return RouteEntity(
        trainId = details.trainId,
        line = details.line,
        route = details.route,
        stationOriginId = details.stationOriginId,
        stationOriginName = details.stationOriginName,
        stationDestinationId = details.stationDestinationId,
        stationDestinationName = details.stationDestinationName,
        arrivesAt = details.arrivesAt,
        stops = routes.map { it.toEntity() },
    )
}

private fun RouteResponse.StopResponse.toEntity(): RouteEntity.Stops {
    return RouteEntity.Stops(
        id = id,
        stationId = stationId,
        stationName = stationName,
        departsAt = departsAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

}