package dev.achmad.data.remote.model.transit

import dev.achmad.data.local.entity.transit.TransitEntity

fun TransitResponse.toEntity(): TransitEntity {
    return TransitEntity(
        transitId = "${this.originStationId}-${this.destinationStationId}",
        originStationId = this.originStationId,
        originStationName = this.originStationName,
        destinationStationId = this.destinationStationId,
        destinationStationName = this.destinationStationName,
        totalStops = this.totalStops.toString(),
        routeGroups = this.routeGroups.map { it.toEntity() }
    )
}

private fun TransitResponse.RouteResponse.toEntity(): TransitEntity.Routes {
    return TransitEntity.Routes(
        line = this.line,
        stations = this.stations.map { it.toEntity() }
    )
}

private fun TransitResponse.RouteResponse.StationResponse.toEntity(): TransitEntity.Routes.Stations {
    return TransitEntity.Routes.Stations(
        stationId = this.stationId,
        stationName = this.stationName
    )
}