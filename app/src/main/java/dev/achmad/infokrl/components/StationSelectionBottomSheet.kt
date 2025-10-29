package dev.achmad.infokrl.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.achmad.domain.station.interactor.SyncStation
import dev.achmad.domain.station.model.Station
import dev.achmad.infokrl.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSelectionBottomSheet(
    syncStationResult: SyncStation.Result,
    stations: List<Station>?,
    searchQuery: String?,
    originStationId: String,
    destinationStationId: String,
    onChangeSearchQuery: (String?) -> Unit,
    onSelectStation: (Station) -> Unit,
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter out already selected stations
    val filteredStations = stations?.filter { station ->
        station.id != originStationId && station.id != destinationStationId
    }

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
        if (syncStationResult is SyncStation.Result.Loading || filteredStations == null) {
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
        } else if (filteredStations.isEmpty()) {
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
            val pinnedStations = filteredStations.filter { it.favorite }.sortedBy { it.favoritePosition }
            val unpinnedStations = filteredStations.filter { !it.favorite }

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

                if (unpinnedStations.isNotEmpty() && pinnedStations.isNotEmpty()) {
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
