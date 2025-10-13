package dev.achmad.comuline.screens.home.station_detail

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.comuline.components.AppBar
import dev.achmad.comuline.util.brighter
import dev.achmad.comuline.util.darken
import dev.achmad.comuline.util.toColor
import dev.achmad.comuline.work.SyncRouteJob
import dev.achmad.core.di.util.injectContext
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

private const val DIVIDER_KEY = "divider_key"

data class StationDetailScreen(
    private val originStationId: String,
    private val destinationStationId: String,
    private val scheduleId: String? = null,
): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { StationDetailScreenModel(originStationId, destinationStationId) }
        val schedules by screenModel.schedules.collectAsState()

        StationDetailScreen(
            onNavigateUp = {
                navigator.pop()
            },
            onClickSchedule = {},
            focusedScheduleId = scheduleId,
            schedules = schedules,
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationDetailScreen(
    onNavigateUp: () -> Unit,
    onRefresh: () -> Unit = {},
    onClickSchedule: (String) -> Unit = {},
    focusedScheduleId: String?,
    schedules: ScheduleGroup?,
) {
    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp
            ) {
                AppBar(
                    titleContent = {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = schedules?.originStation?.name ?: "",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Icon(
                                    modifier = Modifier
                                        .height(20.dp),
                                    imageVector = Icons.AutoMirrored.Default.ArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                )

                                Text(
                                    text = schedules?.destinationStation?.name ?: "",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            val firstSchedule = schedules?.schedules?.firstOrNull()?.schedule
                            if (firstSchedule != null) {
                                val color = firstSchedule.color.toColor()
                                Text(
                                    text = firstSchedule.line,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSystemInDarkTheme()) {
                                        color.brighter(.35f)
                                    } else color.darken(.15f),
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    },
                    navigateUp = onNavigateUp
                )
            }
        },
//        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
        val lazyListState = rememberLazyListState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            when {
                schedules == null -> {
                    // Loading state
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                schedules.schedules.isEmpty() -> {
                    // Empty state
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
                            text = "No upcoming schedules found",
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
                                contentDescription = "Refresh"
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = schedules.schedules,
                            key = { _, item -> item.schedule.id }
                        ) { index, uiSchedule ->
                            ScheduleDetailItem(
                                index = index,
                                lastIndex = schedules.schedules.lastIndex,
                                uiSchedule = uiSchedule,
                                onClick = { onClickSchedule(uiSchedule.schedule.id) },
                            )
                        }
                        item {
                            HorizontalDivider()
                        }
                    }
                    if (focusedScheduleId != null) {
                        LaunchedEffect(focusedScheduleId) {
                            val index = schedules.schedules.indexOfFirst {
                                it.schedule.id == focusedScheduleId
                            }
                            if (index != -1) {
                                lazyListState.animateScrollToItem(index)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleDetailItem(
    index: Int,
    lastIndex: Int,
    uiSchedule: ScheduleGroup.UISchedule,
    onClick: () -> Unit = {},
) {
    val density = LocalDensity.current
    val schedule = uiSchedule.schedule
    val route by uiSchedule.route.collectAsState()
    val color = schedule.color.toColor()
    var height by remember { mutableStateOf(0.dp) }

    Row(
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .height(height)
                .background(color = color)
                .padding(start = 6.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { with(density) { height = it.height.toDp() } }
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
        ) {

            // Departure time and ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Departs at",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = schedule.departsAt.format(
                                DateTimeFormatter.ofPattern("HH:mm")
                            ),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                        )
                        Text(
                            modifier = Modifier.offset(y = (-4).dp),
                            text = uiSchedule.eta,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }

                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Train info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Default.Train,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = schedule.trainId,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text =  (if (route == null) "Unknown" else "${route!!.stops.size}") + " stops",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            if (index != lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

        }
    }
}