package dev.achmad.infokrl.screens.timeline

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
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { TimelineScreenModel(trainId = trainId) }

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
                    val allStops = timelines.currentRoute.stops
                    val formatter = timeFormatter(is24Hour)
                    val now = timelines.currentTime

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        val currentTrainStopIndex = allStops.indexOfLast { stop ->
                            stop.departsAt.isBefore(now) || stop.departsAt.isEqual(now)
                        }

                        itemsIndexed(allStops) { index, stop ->
                            val time = stop.departsAt.format(formatter)

                            val isPassed = stop.departsAt.isBefore(now)
                            val isNextStop = if ( index > 0) {
                                allStops[index - 1].departsAt.isBefore(now) &&
                                stop.departsAt.isAfter(now)
                            } else {
                                false
                            }

                            val isTrainAtStation = index == currentTrainStopIndex
                            val eta = if (!isPassed) {
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
                                isLast = index == allStops.lastIndex,
                                isPassed = isPassed,
                                isNextStop = isNextStop,
                                isTrainAtStation = isTrainAtStation
                            )
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
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
    isPassed: Boolean = false,
    isNextStop: Boolean = false,
    isTrainAtStation: Boolean = false
) {
    val nodeColor = when {
        isPassed -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    val textColor = when {
        isPassed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        isNextStop -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }

    val textWeight = if (isTrainAtStation && !isPassed) FontWeight.Bold else FontWeight.SemiBold

    val lineColor = when {
        isPassed && !isTrainAtStation -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = time ?: "--:--",
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = textWeight
            ),
            modifier = Modifier
                .width(90.dp)
                .offset( y = if (!isTrainAtStation) (-2).dp else 2.dp),
            color = textColor
        )
        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Last node no line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(64.dp)
                        .offset(y = 16.dp)
                        .background(lineColor)
                )
            }
            
            // Node circle
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
            modifier = Modifier
                .weight(1f)
                .offset( y = if (!isTrainAtStation) (-2).dp else 2.dp)
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
                )
            }
        }
    }
}