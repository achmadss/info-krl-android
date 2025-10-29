package dev.achmad.data.local.entity.transit

import dev.achmad.domain.transit.model.Transit

fun TransitEntity.toDomain(): Transit {
    return Transit(
        transitId = transitId,
        originStationId = originStationId,
        originStationName = originStationName,
        destinationStationId = destinationStationId,
        destinationStationName = destinationStationName,
        totalStops = totalStops,
        routeGroups = routeGroups.map { route ->
            Transit.Route(
                line = route.line,
                stations = route.stations.map { station ->
                    Transit.Route.Station(
                        stationId = station.stationId,
                        stationName = station.stationName
                    )
                }
            )
        }
    )
}

fun Transit.toEntity(): TransitEntity {
    return TransitEntity(
        transitId = "${originStationId}-${destinationStationId}",
        originStationId = originStationId,
        originStationName = originStationName,
        destinationStationId = destinationStationId,
        destinationStationName = destinationStationName,
        totalStops = totalStops,
        routeGroups = routeGroups.map { route ->
            TransitEntity.Routes(
                line = route.line,
                stations = route.stations.map { station ->
                    TransitEntity.Routes.Stations(
                        stationId = station.stationId,
                        stationName = station.stationName
                    )
                }
            )
        }
    )
}

fun List<TransitEntity>.toDomain(): List<Transit> {
    return map { it.toDomain() }
}

fun List<Transit>.toEntity(): List<TransitEntity> {
    return map { it.toEntity() }
}