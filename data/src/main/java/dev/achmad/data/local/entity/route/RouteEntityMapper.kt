package dev.achmad.data.local.entity.route

import dev.achmad.core.util.format
import dev.achmad.core.util.toLocalDateTime
import dev.achmad.domain.route.model.Route

fun RouteEntity.toDomain(): Route {
    return Route(
        trainId = trainId,
        line = line,
        route = route,
        stationOriginId = stationOriginId,
        stationOriginName = stationOriginName,
        stationDestinationId = stationDestinationId,
        stationDestinationName = stationDestinationName,
        arrivesAt = arrivesAt.toLocalDateTime(),
        stops = stops.map {
            Route.Stops(
                stationId = it.stationId,
                stationName = it.stationName,
                departsAt = it.departsAt.toLocalDateTime(),
                createdAt = it.createdAt.toLocalDateTime(),
                updatedAt = it.updatedAt.toLocalDateTime()
            )
        }
    )
}

fun Route.toEntity(): RouteEntity {
    return RouteEntity(
        trainId = trainId,
        line = line,
        route = route,
        stationOriginId = stationOriginId,
        stationOriginName = stationOriginName,
        stationDestinationId = stationDestinationId,
        stationDestinationName = stationDestinationName,
        arrivesAt = arrivesAt.format(),
        stops = stops.map {
            RouteEntity.Stops(
                stationId = it.stationId,
                stationName = it.stationName,
                departsAt = it.departsAt.format(),
                createdAt = it.createdAt.format(),
                updatedAt = it.updatedAt.format()
            )
        }
    )
}

fun List<RouteEntity>.toDomain(): List<Route> {
    return map { it.toDomain() }
}

fun List<Route>.toEntity(): List<RouteEntity> {
    return map { it.toEntity() }
}