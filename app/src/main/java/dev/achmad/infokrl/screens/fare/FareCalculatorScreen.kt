package dev.achmad.infokrl.screens.fare

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.domain.fare.interactor.SyncFare
import dev.achmad.domain.fare.model.Fare
import dev.achmad.domain.station.interactor.SyncStation
import dev.achmad.domain.station.model.Station
import dev.achmad.infokrl.R
import dev.achmad.infokrl.components.AppBar
import dev.achmad.infokrl.components.SearchToolbar
import kotlinx.coroutines.launch

object FareCalculatorScreen: Screen {
    private fun readResolve(): Any = FareCalculatorScreen

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { FareCalculatorScreenModel() }
        val syncFareResult by screenModel.syncFareResult.collectAsState()
        val syncStationResult by screenModel.syncStationResult.collectAsState()
        val fare by screenModel.fare.collectAsState()
        val originStation by screenModel.originStation.collectAsState()
        val destinationStation by screenModel.destinationStation.collectAsState()
        val stations by screenModel.stations.collectAsState()
        val searchQuery by screenModel.searchQuery.collectAsState()

        FareCalculatorScreen(
            onNavigateUp = { navigator.pop() },
            syncFareResult = syncFareResult,
            syncStationResult = syncStationResult,
            fare = fare,
            originStation = originStation,
            destinationStation = destinationStation,
            stations = stations,
            searchQuery = searchQuery,
            onChangeOrigin = { screenModel.updateOriginStation(it) },
            onChangeDestination = { screenModel.updateDestinationStation(it) },
            onChangeSearchQuery = { screenModel.search(it) },
            onTryAgain = {
                screenModel.fetchFare()
            },
            onTryAgainStations = {
                screenModel.fetchStations()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FareCalculatorScreen(
    syncFareResult: SyncFare.Result?,
    syncStationResult: SyncStation.Result,
    fare: Fare?,
    originStation: Pair<String, String>,
    destinationStation: Pair<String, String>,
    stations: List<Station>?,
    searchQuery: String?,
    onChangeOrigin: (Pair<String, String>) -> Unit,
    onChangeDestination: (Pair<String, String>) -> Unit,
    onChangeSearchQuery: (String?) -> Unit,
    onTryAgain: () -> Unit,
    onTryAgainStations: () -> Unit,
    onNavigateUp: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var stationSelectionTarget by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    BackHandler(stationSelectionTarget != null) {
        stationSelectionTarget = null
        onChangeSearchQuery(null)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = "Fare Calculator", // TODO string resource
                navigateUp = onNavigateUp,
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Origin station textbox
            StationTextField(
                label = "From", // TODO string resource
                value = originStation.second,
                onClick = {
                    stationSelectionTarget = "origin"
                }
            )

            // Destination station textbox
            StationTextField(
                label = "To", // TODO string resource
                value = destinationStation.second,
                onClick = {
                    stationSelectionTarget = "destination"
                }
            )

            // Calculate button
            val calculateButtonEnabled =
                originStation.first.isNotBlank() &&
                destinationStation.first.isNotBlank() &&
                syncFareResult !is SyncFare.Result.Loading
            Button(
                onClick = onTryAgain,
                modifier = Modifier.fillMaxWidth(),
                enabled = calculateButtonEnabled
            ) {
                Text(text = "Calculate") // TODO string resource
            }

            // Fare display section
            if (syncFareResult is SyncFare.Result.Loading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (syncFareResult is SyncFare.Result.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.error_something_wrong),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else if (fare != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Fare", // TODO string resource
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rp ${
                                if (fare.fare % 1.0 == 0.0) {
                                    fare.fare.toLong()
                                } else {
                                    fare.fare
                                }
                            }",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Bottom sheet for station selection
        if (stationSelectionTarget != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    stationSelectionTarget = null
                    onChangeSearchQuery(null)
                },
                sheetState = sheetState
            ) {
                StationSelectionBottomSheet(
                    modifier = Modifier.fillMaxSize(),
                    syncStationResult = syncStationResult,
                    stations = stations,
                    searchQuery = searchQuery,
                    onChangeSearchQuery = onChangeSearchQuery,
                    onSelectStation = { station: Station ->
                        when (stationSelectionTarget) {
                            "origin" -> onChangeOrigin(Pair(station.id, station.name))
                            "destination" -> onChangeDestination(Pair(station.id, station.name))
                        }
                        scope.launch {
                            sheetState.hide()
                            stationSelectionTarget = null
                            onChangeSearchQuery(null)
                        }
                    },
                    onTryAgain = onTryAgainStations
                )
            }
        }
    }
}

@Composable
private fun StationTextField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value.ifBlank { "Select station" }, // TODO string resource
                style = MaterialTheme.typography.bodyLarge,
                color = if (value.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationSelectionBottomSheet(
    syncStationResult: SyncStation.Result,
    stations: List<Station>?,
    searchQuery: String?,
    onChangeSearchQuery: (String?) -> Unit,
    onSelectStation: (Station) -> Unit,
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Search toolbar
        SearchToolbar(
            windowInsets = WindowInsets.navigationBars,
            backgroundColor = BottomSheetDefaults.ContainerColor,
            titleContent = {
                Text(
                    text = "Select Station", // TODO string resource
                    style = MaterialTheme.typography.titleLarge
                )
            },
            searchQuery = searchQuery,
            onChangeSearchQuery = onChangeSearchQuery
        )

        HorizontalDivider()

        // Station list
        if (syncStationResult is SyncStation.Result.Loading || stations == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (syncStationResult is SyncStation.Result.Error) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
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
        } else if (stations.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
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
        } else {
            val pinnedStations = stations.filter { it.favorite }.sortedBy { it.favoritePosition }
            val unpinnedStations = stations.filter { !it.favorite }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (pinnedStations.isNotEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            text = stringResource(R.string.pinned),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }

                    itemsIndexed(
                        items = pinnedStations,
                        key = { _, item -> "pinned_${item.id}" }
                    ) { index, station ->
                        StationListItem(
                            station = station,
                            onClick = { onSelectStation(station) }
                        )
                        if (index != pinnedStations.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }

                if (unpinnedStations.isNotEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            text = stringResource(R.string.all),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }

                    itemsIndexed(
                        items = unpinnedStations,
                        key = { _, item -> "unpinned_${item.id}" }
                    ) { index, station ->
                        StationListItem(
                            station = station,
                            onClick = { onSelectStation(station) }
                        )
                        if (index != unpinnedStations.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationListItem(
    station: Station,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = station.name,
            style = MaterialTheme.typography.bodyLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}