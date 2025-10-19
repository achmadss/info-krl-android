package dev.achmad.infokrl.screens.stations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.model.Station
import dev.achmad.infokrl.R
import dev.achmad.infokrl.base.ApplicationPreference
import dev.achmad.infokrl.components.AppBarTitle
import dev.achmad.infokrl.components.DraggableItem
import dev.achmad.infokrl.components.SearchToolbar
import dev.achmad.infokrl.components.dragContainer
import dev.achmad.infokrl.components.rememberDragDropState
import dev.achmad.infokrl.work.SyncStationJob

object StationsScreen: Screen {
    private fun readResolve(): Any = StationsScreen

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val appContext = LocalContext.current.applicationContext
        val screenModel = rememberScreenModel { StationsScreenModel() }
        val searchQuery by screenModel.searchQuery.collectAsState()
        val filteredStations by screenModel.filteredStations.collectAsState()
        val applicationPreference by remember { injectLazy<ApplicationPreference>() }
        val syncState by SyncStationJob.subscribeState(
            context = appContext,
            scope = screenModel.screenModelScope
        ).collectAsState()

        LaunchedEffect(Unit) {
            if (!applicationPreference.hasFetchedStations().get()) {
                SyncStationJob.start(appContext)
            }
        }

        StationsScreen(
            loading = when {
                syncState == WorkInfo.State.ENQUEUED ||
                        syncState == WorkInfo.State.RUNNING -> true
                else -> false
            },
            error = when(syncState) {
                WorkInfo.State.FAILED, WorkInfo.State.BLOCKED, WorkInfo.State.CANCELLED -> true
                else -> false
            },
            stations = filteredStations,
            searchQuery = searchQuery,
            onChangeSearchQuery = { query ->
                screenModel.search(query)
            },
            onTogglePin = { station ->
                screenModel.toggleFavorite(station)
            },
            onNavigateUp = {
                navigator.pop()
            },
            onTryAgain = {
                SyncStationJob.start(appContext)
            },
            onClickStation = { station ->
                screenModel.toggleFavorite(station)
            },
            onReorderFavorites = { fromIndex, toIndex ->
                screenModel.reorderFavorites(fromIndex, toIndex)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationsScreen(
    loading: Boolean,
    error: Boolean,
    stations: List<Station>?,
    searchQuery: String?,
    onChangeSearchQuery: (String?) -> Unit,
    onTogglePin: (Station) -> Unit,
    onNavigateUp: () -> Unit,
    onTryAgain: () -> Unit,
    onClickStation: (Station) -> Unit,
    onReorderFavorites: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp
            ) {
                SearchToolbar(
                    titleContent = { AppBarTitle(stringResource(R.string.stations)) },
                    searchQuery = searchQuery,
                    onChangeSearchQuery = onChangeSearchQuery,
                    navigateUp = { onNavigateUp() },
                )
            }
        }
    ) { contentPadding ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        if (stations?.isEmpty() == true) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(R.string.no_station_found, searchQuery ?: ""),
                    textAlign = TextAlign.Center,
                )
            }
            return@Scaffold
        }

        if (error) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(R.string.error_something_wrong),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onTryAgain) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.action_try_again))
                    }
                }
            }
            return@Scaffold
        }

        val pinnedStations = stations
            ?.filter { it.favorite }
            ?.sortedBy { it.favoritePosition }
            ?: emptyList()

        val unpinnedStations = stations
            ?.filter { !it.favorite }
            ?: emptyList()

        val listState = rememberLazyListState()

        val enableDragDrop = searchQuery.isNullOrBlank() && pinnedStations.isNotEmpty()
        val dragDropState = if (enableDragDrop) {
            rememberDragDropState(
                lazyListState = listState,
                onMove = { fromIndex, toIndex ->
                    val fromListIndex = fromIndex - 1
                    val toListIndex = toIndex - 1

                    if (fromListIndex >= 0 && fromListIndex < pinnedStations.size &&
                        toListIndex >= 0 && toListIndex < pinnedStations.size) {
                        onReorderFavorites(fromListIndex, toListIndex)
                    }
                }
            ).apply {
                minDraggableIndex = 1 // first draggable item index (after header)
                maxDraggableIndex = pinnedStations.size
            }
        } else null

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
            state = listState,
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            if (pinnedStations.isNotEmpty()) {
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        text = if (enableDragDrop) {
                            stringResource(R.string.pinned_hold_to_drag)
                        } else {
                            stringResource(R.string.pinned)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }

                itemsIndexed(
                    items = pinnedStations,
                    key = { _, item -> item.id.plus("_pinned") }
                ) { index, station ->
                    if (enableDragDrop && dragDropState != null) {
                        DraggableItem(
                            dragDropState = dragDropState,
                            index = index + 1 // + 1 to take into account header, not ideal but works for now
                        ) { isDragging ->
                            StationItem(
                                station = station,
                                onTogglePin = { onTogglePin(station) },
                                onClick = { onClickStation(station) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .dragContainer(dragDropState, station.id.plus("_pinned")),
                                isDragging = isDragging,
                                showDragHandle = true
                            )
                        }
                    } else {
                        StationItem(
                            station = station,
                            onTogglePin = { onTogglePin(station) },
                            onClick = { onClickStation(station) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (index != pinnedStations.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }

            if (!stations.isNullOrEmpty()) {
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        text = stringResource(R.string.all),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            itemsIndexed(
                items = unpinnedStations,
                key = { _, item -> item.id }
            ) { index, station ->
                StationItem(
                    station = station,
                    onTogglePin = { onTogglePin(station) },
                    onClick = { onClickStation(station) },
                    modifier = Modifier.fillMaxWidth().animateItem()
                )
                if (index != unpinnedStations.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun StationItem(
    station: Station,
    onTogglePin: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    showDragHandle: Boolean = false
) {
    val icon = if (station.favorite) R.drawable.push_pin else R.drawable.push_pin_outline
    val backgroundColor = if (isDragging) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shadowElevation = if (isDragging) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showDragHandle) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = stringResource(R.string.content_desc_drag_handle),
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = station.name,
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = onTogglePin) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}