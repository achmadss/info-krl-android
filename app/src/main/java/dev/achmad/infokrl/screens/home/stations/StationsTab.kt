package dev.achmad.infokrl.screens.home.stations

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Train
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.achmad.domain.station.interactor.SyncStation
import dev.achmad.domain.station.model.Station
import dev.achmad.infokrl.R
import dev.achmad.infokrl.components.AppBarTitle
import dev.achmad.infokrl.components.SearchToolbar
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

object StationsTab: Tab {
    private fun readResolve(): Any = StationsTab

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            return TabOptions(
                index = 2u,
                title = "Stations", // TODO string resource
                icon = rememberVectorPainter(
                    when {
                        isSelected -> Icons.Default.Train
                        else -> Icons.Outlined.Train
                    }
                )
            )
        }

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { StationsTabScreenModel() }
        val searchQuery by screenModel.searchQuery.collectAsState()
        val stations by screenModel.stations.collectAsState()
        val syncStationResult by screenModel.syncStationResult.collectAsState()

        BackHandler(searchQuery != null) {
            screenModel.search(null)
        }

        StationsTab(
            loading = syncStationResult is SyncStation.Result.Loading || stations == null,
            error = syncStationResult is SyncStation.Result.Error,
            stations = stations,
            searchQuery = searchQuery,
            onChangeSearchQuery = { query ->
                screenModel.search(query)
            },
            onTogglePin = { station ->
                screenModel.toggleFavorite(station)
            },
            onTryAgain = {
                screenModel.fetchStations()
            },
            onReorder = { station, newPosition ->
                screenModel.reorderFavorite(station, newPosition)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationsTab(
    loading: Boolean,
    error: Boolean,
    stations: List<Station>?,
    searchQuery: String?,
    onChangeSearchQuery: (String?) -> Unit,
    onTogglePin: (Station) -> Unit,
    onTryAgain: () -> Unit,
    onReorder: (Station, Int) -> Unit,
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

        val pinnedStationsFromDb = stations
            ?.filter { it.favorite }
            ?.sortedBy { it.favoritePosition }
            ?: emptyList()

        val unpinnedStations = stations
            ?.filter { !it.favorite }
            ?: emptyList()

        val listState = rememberLazyListState()
        val enableDragDrop = searchQuery.isNullOrBlank() && pinnedStationsFromDb.isNotEmpty()

        // Mutable state list for drag reordering
        val pinnedStations = remember { pinnedStationsFromDb.toMutableStateList() }

        val reorderableLazyListState = rememberReorderableLazyListState(listState) { from, to ->
            // Account for header (index 0), so pinned items are indices 1 to pinnedStations.size
            if (from.index > 0 && from.index <= pinnedStations.size &&
                to.index > 0 && to.index <= pinnedStations.size) {
                val fromIndex = from.index - 1
                val toIndex = to.index - 1

                // Reorder in UI list immediately
                val item = pinnedStations.removeAt(fromIndex)
                pinnedStations.add(toIndex, item)

                // Update database
                onReorder(item, toIndex)
            }
        }

        // Sync pinnedStations with database when not dragging
        LaunchedEffect(pinnedStationsFromDb) {
            if (!reorderableLazyListState.isAnyItemDragging) {
                pinnedStations.clear()
                pinnedStations.addAll(pinnedStationsFromDb)
            }
        }

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
                    key = { _, item -> "pinned_${item.id}" }
                ) { index, station ->
                    ReorderableItem(reorderableLazyListState, key = "pinned_${station.id}") { isDragging ->
                        val icon = if (station.favorite) R.drawable.push_pin else R.drawable.push_pin_outline
                        val backgroundColor = if (isDragging) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                        Column(modifier = Modifier.fillMaxWidth().animateItem()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (enableDragDrop) {
                                            Modifier.longPressDraggableHandle()
                                        } else Modifier
                                    ),
                                color = backgroundColor,
                                shadowElevation = if (isDragging) 4.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clickable(enabled = !reorderableLazyListState.isAnyItemDragging) {
                                            onTogglePin(station)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (enableDragDrop) {
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
                                    IconButton(
                                        onClick = { onTogglePin(station) },
                                        enabled = !reorderableLazyListState.isAnyItemDragging
                                    ) {
                                        Icon(
                                            painter = painterResource(icon),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            if (index != pinnedStations.lastIndex) {
                                HorizontalDivider()
                            }
                        }
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
                key = { _, item -> "unpinned_${item.id}" }
            ) { index, station ->
                StationItem(
                    station = station,
                    onTogglePin = { onTogglePin(station) },
                    onClick = { onTogglePin(station) },
                    modifier = Modifier.fillMaxWidth().animateItem(),
                    enabled = !reorderableLazyListState.isAnyItemDragging
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
    showDragHandle: Boolean = false,
    enabled: Boolean = true
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
            modifier = Modifier
                .clickable(enabled = enabled) { onClick() }
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
            IconButton(
                onClick = onTogglePin,
                enabled = enabled
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}