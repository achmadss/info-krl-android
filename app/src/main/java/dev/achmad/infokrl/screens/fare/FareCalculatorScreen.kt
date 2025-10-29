package dev.achmad.infokrl.screens.fare

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
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
import dev.achmad.infokrl.components.StationSelectionBottomSheet
import dev.achmad.infokrl.components.StationTextField
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