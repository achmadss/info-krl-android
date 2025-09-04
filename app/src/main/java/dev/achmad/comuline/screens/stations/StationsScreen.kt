package dev.achmad.comuline.screens.stations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.comuline.base.ApplicationPreference
import dev.achmad.comuline.components.AppBarTitle
import dev.achmad.comuline.components.SearchToolbar
import dev.achmad.comuline.work.SyncStationJob
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.model.Station

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
            if (applicationPreference.isFirstRun().get()) {
                SyncStationJob.start(appContext)
            }
        }

        StationsScreen(
            loading = syncState?.isFinished?.not() ?: false,
            stations = filteredStations,
            searchQuery = searchQuery,
            onChangeSearchQuery = { query ->
                screenModel.search(query)
            },
            onToggleFavorite = { station ->
                screenModel.toggleFavorite(station)
            },
            onNavigateUp = {
                navigator.pop()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationsScreen(
    loading: Boolean,
    stations: List<Station>,
    searchQuery: String?,
    onChangeSearchQuery: (String?) -> Unit,
    onToggleFavorite: (Station) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp
            ) {
                SearchToolbar(
                    titleContent = { AppBarTitle("Stations") },
                    searchQuery = searchQuery,
                    onChangeSearchQuery = onChangeSearchQuery,
                    navigateUp = {
                        onNavigateUp()
                    },
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
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            itemsIndexed(
                items = stations,
                key = { _, item -> item.id }
            ) { index, station ->
                StationItem(
                    station = station,
                    onToggleFavorite = { onToggleFavorite(station) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (index != stations.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun StationItem(
    station: Station,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when {
        station.favorite -> Icons.Default.Favorite
        else -> Icons.Default.FavoriteBorder
    }
    Row(
        modifier = modifier
            .clickable { onToggleFavorite() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = station.name,
            style = MaterialTheme.typography.bodyLarge,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(
            onClick = { onToggleFavorite() }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}