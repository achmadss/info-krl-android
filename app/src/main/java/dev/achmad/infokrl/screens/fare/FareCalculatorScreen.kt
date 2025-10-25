package dev.achmad.infokrl.screens.fare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import dev.achmad.infokrl.R
import dev.achmad.infokrl.components.AppBar

object FareCalculatorScreen: Screen {
    private fun readResolve(): Any = FareCalculatorScreen

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { FareCalculatorScreenModel() }
        val syncFareResult by screenModel.syncFareResult.collectAsState()
        val fare by screenModel.fare.collectAsState()
        val originStation by screenModel.originStation.collectAsState()
        val destinationStation by screenModel.destinationStation.collectAsState()

        FareCalculatorScreen(
            onNavigateUp = { navigator.pop() },
            loading = syncFareResult is SyncFare.Result.Loading,
            error = syncFareResult is SyncFare.Result.Error,
            fare = fare,
            originStation = originStation,
            destinationStation = destinationStation,
            onChangeOrigin = { screenModel.updateOriginStation(it) },
            onChangeDestination = { screenModel.updateDestinationStation(it) },
            onTryAgain = {
                screenModel.fetchFare()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FareCalculatorScreen(
    loading: Boolean,
    error: Boolean,
    fare: Fare?,
    originStation: Pair<String, String>,
    destinationStation: Pair<String, String>,
    onChangeOrigin: (Pair<String, String>) -> Unit,
    onChangeDestination: (Pair<String, String>) -> Unit,
    onTryAgain: () -> Unit,
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = "Fare Calculator", // TODO string resource
                navigateUp = onNavigateUp,
            )
        }
    ) { contentPadding ->
        // TODO from and to textbox section

        // TODO bottom sheet section

        // TODO fare section, showing the fare in number if not null, should be including the loading and error
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
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
    }
}