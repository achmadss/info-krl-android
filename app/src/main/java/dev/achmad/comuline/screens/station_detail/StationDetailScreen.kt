package dev.achmad.comuline.screens.station_detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.domain.model.Station

data class StationDetailScreen(
    private val stationId: String,
): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val appContext = LocalContext.current.applicationContext
    }

}

@Composable
private fun StationDetailScreen(
    loading: Boolean,
    error: Boolean,
    station: Station,
) {

}