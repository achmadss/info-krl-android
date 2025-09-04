package dev.achmad.comuline.screens.station_detail

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

data class StationDetailScreen(
    val stationId: String,
): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

    }

}