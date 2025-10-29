package dev.achmad.domain.transit.model

data class Transit(
    val transitId: String,
    val originStationId: String,
    val originStationName: String,
    val destinationStationId: String,
    val destinationStationName: String,
    val totalStops: String,
    val routeGroups: List<Route>,
) {
    data class Route(
        val line: String,
        val stations: List<Station>,
    ) {
        data class Station(
            val stationId: String,
            val stationName: String,
        )
    }
}