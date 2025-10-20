package dev.achmad.infokrl.screens.schedules.timelinescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.core.di.util.injectContext
import dev.achmad.core.di.util.injectLazy
import dev.achmad.infokrl.R
import dev.achmad.infokrl.base.ApplicationPreference
import dev.achmad.infokrl.components.AppBar
import dev.achmad.infokrl.util.collectAsState
import dev.achmad.infokrl.util.etaString
import dev.achmad.infokrl.util.timeFormatter

data class TimelineScreen(
    private val trainId: String,
    private val originStationId: String,
    private val destinationStationId: String,
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { TimelineScreenModel(
            trainId = trainId,
            originStationId = originStationId,
            destinationStationId = destinationStationId,
        ) }

        val applicationPreference by injectLazy<ApplicationPreference>()
        val is24Hour by applicationPreference.is24HourFormat().collectAsState()
        val timelines by screenModel.timelineGroup.collectAsState()

        TimelineScreen(
            onNavigateUp = { navigator.pop() },
            onRefresh = { screenModel.refresh() },
            is24Hour = is24Hour,
            trainId = trainId,
            timelines = timelines
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineScreen(
    onNavigateUp: () -> Unit,
    onRefresh: () -> Unit,
    is24Hour: Boolean,
    trainId: String,
    timelines: TimelineGroup?
) {

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp
            ) {
                AppBar(
                    title = "${stringResource(R.string.train)} $trainId",
                    navigateUp = onNavigateUp,
                )
            }
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            when {
                timelines?.currentRoute?.stops?.isEmpty() == true -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = stringResource(R.string.no_upcoming_schedules),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        IconButton(
                            onClick = {
                                onRefresh()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.action_refresh)
                            )
                        }
                    }
                }
                timelines != null -> {
                    // Use currentRoute stops as the primary source
                    val allStops = timelines.currentRoute.stops
                    val formatter = timeFormatter(is24Hour)

                    val now = timelines.currentTime

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Find the latest station reached (train's current position)
                        val currentTrainStopIndex = allStops.indexOfLast { stop ->
                            stop.departsAt.isBefore(now) || stop.departsAt.isEqual(now)
                        }

                        // Show all stops from current route
                        itemsIndexed(allStops) { index, stop ->
                            // Check if this stop has real time data or is a placeholder (missing stop)
                            val hasTimeData = stop.departsAt.isBefore(timelines.currentRoute.arrivesAt)

                            val time = if (hasTimeData) stop.departsAt.format(formatter) else null

                            val isPassed = if (hasTimeData) stop.departsAt.isBefore(now) else false
                            val isNextStop = if (hasTimeData && index > 0) {
                                allStops[index - 1].departsAt.isBefore(now) &&
                                stop.departsAt.isAfter(now)
                            } else {
                                false
                            }

                            // Show train icon ONLY at the latest reached station (current position)
                            val isTrainAtStation = if (hasTimeData) index == currentTrainStopIndex else false

                            // Calculate ETA only for stops with real time data
                            val eta = if (hasTimeData && !isPassed) {
                                etaString(
                                    context = injectContext(),
                                    now = now,
                                    target = stop.departsAt,
                                    compactMode = true
                                )
                            } else null

                            TimelineNode(
                                time = time,
                                stationName = stop.stationName,
                                eta = eta,
                                isLast = false,
                                isActive = hasTimeData, // Inactive if no time data
                                isPassed = isPassed,
                                isNextStop = isNextStop,
                                isTrainAtStation = isTrainAtStation
                            )
                        }

                        // Add destination station as final item
                        item {
                            val isPassed = timelines.currentRoute.arrivesAt.isBefore(now)
                            val lastStop = allStops.lastOrNull()
                            val isNextStop = if (lastStop != null) {
                                lastStop.departsAt.isBefore(now) && timelines.currentRoute.arrivesAt.isAfter(now)
                            } else {
                                false
                            }

                            // Check if train is at destination (only if all stops are passed)
                            val isTrainAtStation = if (currentTrainStopIndex == allStops.size - 1 && !timelines.currentRoute.arrivesAt.isAfter(now)) {
                                true
                            } else {
                                false
                            }

                            // Calculate ETA for destination
                            val eta = if (!isPassed) {
                                etaString(
                                    context = injectContext(),
                                    now = now,
                                    target = timelines.currentRoute.arrivesAt,
                                    compactMode = true
                                )
                            } else null

                            TimelineNode(
                                time = timelines.currentRoute.arrivesAt.format(formatter),
                                stationName = timelines.destinationStation.name,
                                eta = eta,
                                isLast = true,
                                isActive = true,
                                isPassed = isPassed,
                                isNextStop = isNextStop,
                                isTrainAtStation = isTrainAtStation
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun TimelineNode(
    modifier: Modifier = Modifier,
    time: String?,
    stationName: String,
    eta: String? = null,
    isLast: Boolean = false,
    isActive: Boolean = true,
    isPassed: Boolean = false,
    isNextStop: Boolean = false,
    isTrainAtStation: Boolean = false
) {
    // Determine color based on state
    val nodeColor = when {
        !isActive -> MaterialTheme.colorScheme.outline
        isPassed -> MaterialTheme.colorScheme.primary // Passed station - blue
        else -> MaterialTheme.colorScheme.outline // Future station - grey
    }

    val textColor = when {
        !isActive -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        isPassed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        isNextStop -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }

    val textWeight = if (isTrainAtStation && !isPassed) FontWeight.Bold else FontWeight.SemiBold

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = time ?: "--:--",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = textWeight
            ),
            modifier = Modifier.width(90.dp),
            color = textColor
        )
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Line segment AFTER this node (going to next station)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(64.dp)
                        .offset(y = 16.dp)
                        .background(
                            if (isPassed && !isTrainAtStation) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                        )
                )
            }
            
            // Node circle - show train icon inside if train is at or past this station
            if (isTrainAtStation) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(nodeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Train,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(nodeColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stationName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = textWeight
                ),
                color = textColor
            )


            // Display ETA if available
            if (eta != null) {
                Text(
                    text = eta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}