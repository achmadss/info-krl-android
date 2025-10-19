package dev.achmad.infokrl.util

import dev.achmad.domain.model.Route

fun calculateStopsCount(
    route: Route?,
    originStationId: String,
): Int? {
    if (route == null) return null

    val stopStationIds = route.stops.map { it.stationId }
    val bstStationsIds = listOf("SUDB", "DU", "RW", "BPR")

    return stopStationIds
        .indexOf(originStationId)
        .takeIf { it != -1 }
        ?.let { index -> stopStationIds.drop(index + 1) }
        ?.let { remainingStops ->
            if (route.line.contains("BST")) {
                remainingStops.filter { stationId -> stationId in bstStationsIds }
            } else remainingStops
        }
        ?.size
}