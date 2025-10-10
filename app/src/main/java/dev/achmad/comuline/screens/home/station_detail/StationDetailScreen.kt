package dev.achmad.comuline.screens.home.station_detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.comuline.components.AppBar

data class StationDetailScreen(
    private val originStationId: String,
    private val destinationStationId: String,
    private val scheduleId: String? = null,
): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { StationDetailScreenModel(destinationStationId) }

        StationDetailScreen(
            onNavigateUp = {
                navigator.pop()
            },
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationDetailScreen(
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppBar(
                title = null,
                navigateUp = onNavigateUp
            )
        }
    ) { contentPadding ->
        if (true) { // TODO if data not ready yet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }


    }
}