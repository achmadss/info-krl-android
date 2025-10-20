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

/**
 * Filters route stops to only include stations from origin onwards.
 */
fun filterRouteStops(
    route: Route,
    originStationId: String
): Route {
    val filteredStops = route.stops.let { stops ->
        val originIndex = stops.indexOfFirst { it.stationId == originStationId }

        when {
            originIndex == -1 -> stops // Origin not found, return all stops
            else -> stops.subList(originIndex, stops.size) // Include all stops from origin onwards
        }
    }

    return route.copy(stops = filteredStops)
}

fun mergeRouteStops(
    currentRoute: Route,
    maxStopRoute: Route
): Route {
    val currentStops = currentRoute.stops
    val maxStops = maxStopRoute.stops

    // If current route has same or more stops than max, just use current
    if (currentStops.size >= maxStops.size) {
        return currentRoute
    }

    // Current route is missing stops - merge them
    val currentStationIds = currentStops.map { it.stationId }.toSet()
    val mergedStops = mutableListOf<Route.Stops>()

    // Add all stops from current route first (these have real time data)
    mergedStops.addAll(currentStops)

    // Find missing stops from maxStopRoute and add them without time data
    val missingStops = maxStops.filter { maxStop ->
        maxStop.stationId !in currentStationIds
    }.map { maxStop ->
        // Create a stop with the station info but use the final arrival time
        // This indicates the stop exists but we don't have timing data
        Route.Stops(
            id = maxStop.id,
            stationId = maxStop.stationId,
            stationName = maxStop.stationName,
            departsAt = currentRoute.arrivesAt, // Use final arrival time as placeholder
            createdAt = maxStop.createdAt,
            updatedAt = maxStop.updatedAt
        )
    }

    // Add missing stops after the current stops
    mergedStops.addAll(missingStops)

    return currentRoute.copy(stops = mergedStops)
}
