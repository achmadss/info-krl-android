package dev.achmad.infokrl.screens.fare

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import dev.achmad.infokrl.components.FromToSelector
import dev.achmad.infokrl.components.StationSelectionBottomSheet
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
            onTryAgain = { screenModel.fetchFare() },
            onTryAgainStations = { screenModel.fetchStations() }
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
                title = stringResource(R.string.fare_calculator),
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
            FromToSelector(
                fromLabel = stringResource(R.string.from),
                fromValue = originStation.second,
                onFromValueChanged = {
                    stationSelectionTarget = "origin"
                },
                toLabel = stringResource(R.string.to),
                toValue = destinationStation.second,
                onToValueChanged = {
                    stationSelectionTarget = "destination"
                },
                onClickSwapButton = {
                    val temp = originStation
                    onChangeOrigin(destinationStation)
                    onChangeDestination(temp)
                },
                onBothFilledChanged = { _, _ ->
                    onTryAgain()
                }
            )

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
                // Total Fare Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RectangleShape
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.total_fare),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
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

                // Journey Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RectangleShape
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.journey_details),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Route,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp).rotate(90f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.distance),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${fare.distance} km",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
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
                    originStationId = originStation.first,
                    destinationStationId = destinationStation.first,
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
                    onTryAgain = onTryAgainStations,
                )
            }
        }
    }
}