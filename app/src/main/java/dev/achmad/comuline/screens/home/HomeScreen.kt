package dev.achmad.comuline.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.comuline.components.TabContent
import dev.achmad.comuline.components.TabbedScreen
import dev.achmad.domain.model.Schedule
import dev.achmad.domain.model.Station

object HomeScreen: Screen {
    private fun readResolve(): Any = HomeScreen

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val appContext = LocalContext.current.applicationContext
        val screenModel = rememberScreenModel { HomeScreenModel() }
        val searchQuery by screenModel.searchQuery.collectAsState()
        val destinationGroups by screenModel.destinationGroups.collectAsState()
        val favoriteStations by screenModel.favoriteStations.collectAsState()

        LaunchedEffect(Unit) {
            screenModel.refreshStation(appContext)
        }

        LaunchedEffect(favoriteStations) {
            if (favoriteStations.isNotEmpty()) {
                screenModel.refreshSchedule(appContext)
            }
        }

        HomeScreen(
            destinationGroups = destinationGroups,
            searchQuery = searchQuery,
            onChangeSearchQuery = { query ->
                screenModel.search(query)
            }
        )

    }

}

@Composable
fun HomeScreen(
    destinationGroups: List<DestinationGroup>,
    searchQuery: String?,
    onChangeSearchQuery: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (destinationGroups.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Train,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Track your favorite stations",
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {

                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add Station"
                    )
                }
            }
        }
        return
    }

    TabbedScreen(
        title = "Comuline",
        tabs = destinationGroups.map { group ->
            TabContent(
                title = group.station.name,
                searchEnabled = true,
                actions = listOf(),
                content = { contentPadding, snackbarHostState ->
                    val schedules by group.schedules.collectAsState()
                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(schedules) { schedule ->
                            Text(schedule.id)
                        }
                    }
                }
            )
        },
        searchQuery = searchQuery,
        onChangeSearchQuery = onChangeSearchQuery,
    )
}