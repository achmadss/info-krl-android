package dev.achmad.comuline.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.work.WorkInfo
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.comuline.R
import dev.achmad.comuline.components.AppBar
import dev.achmad.comuline.components.AppBarActions
import dev.achmad.comuline.components.AppBarTitle
import dev.achmad.comuline.components.SearchToolbar
import dev.achmad.comuline.components.TabText
import dev.achmad.comuline.screens.home.station_detail.StationDetailScreen
import dev.achmad.comuline.screens.stations.StationsScreen
import dev.achmad.comuline.util.brighter
import dev.achmad.comuline.util.darken
import dev.achmad.comuline.util.toColor
import dev.achmad.comuline.work.SyncScheduleJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

data class TabContent(
    val title: String,
    val badgeNumber: Int? = null,
    val searchEnabled: Boolean = false,
    val actions: List<AppBar.AppBarAction> = listOf(),
    val content: @Composable (contentPadding: PaddingValues, snackbarHostState: SnackbarHostState) -> Unit,
)

object HomeScreen: Screen {
    private fun readResolve(): Any = HomeScreen

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { HomeScreenModel() }
        val destinationGroups by screenModel.destinationGroups.collectAsState()
        val focusedStationId by screenModel.focusedStationId.collectAsState()

        LaunchedEffect(Unit) {
            screenModel.fetchSchedules()
        }

        LaunchedEffect(destinationGroups) {
            if (destinationGroups.isNotEmpty()) {
                val initialStationId = focusedStationId ?: destinationGroups.firstOrNull()?.station?.id
                initialStationId?.let { screenModel.onTabFocused(it) }
            }
        }

        HomeScreen(
            syncScope = screenModel.screenModelScope,
            destinationGroups = destinationGroups,
            focusedStationId = focusedStationId,
            onTabFocused = { stationId ->
                screenModel.onTabFocused(stationId)
            },
            onClickAddStation = {
                navigator.push(StationsScreen)
            },
            onClickStationDetail = { route, trainId ->
                navigator.push(
                    StationDetailScreen(
                        route = route,
                        trainId = trainId
                    )
                )
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun HomeScreen(
    syncScope: CoroutineScope,
    destinationGroups: List<DestinationGroup>,
    focusedStationId: String?,
    onTabFocused: (String) -> Unit,
    onClickAddStation: () -> Unit,
    onClickStationDetail: (String, String) -> Unit,
) {
    val applicationContext = LocalContext.current.applicationContext
    var searchQuery by rememberSaveable { mutableStateOf<String?>(null) }
    var searchResults by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val syncStates = destinationGroups.associate { group ->
        group.station.id to remember(group.station.id) {
            SyncScheduleJob.subscribeState(
                context = applicationContext,
                scope = syncScope,
                stationId = group.station.id
            )
        }.collectAsState()
    }
    val tabs = mapTabContents(
        destinationGroups = destinationGroups,
        syncStates = syncStates,
        searchQuery = searchQuery,
        searchResults = searchResults,
        onClickAddStation = onClickAddStation,
        onClickStationDetail = onClickStationDetail
    )
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val initialPage = remember(destinationGroups, focusedStationId) {
        if (focusedStationId != null) {
            destinationGroups
                .indexOfFirst { it.station.id == focusedStationId }
                .takeIf { it >= 0 } ?: 0
        } else 0
    }
    val pagerState = remember(tabs.size) {
        PagerState(
            currentPage = initialPage,
            pageCount = { tabs.size }
        )
    }

    LaunchedEffect(searchQuery, destinationGroups) {
        snapshotFlow { searchQuery?.uppercase() }
            .debounce(300)
            .collect { query ->
                if (!query.isNullOrEmpty()) {
                    val results = destinationGroups.associate { destinationGroup ->
                        val matchCount = destinationGroup.scheduleGroup.value
                            ?.count { scheduleGroup ->
                                // Only count if destination matches AND has upcoming schedules
                                scheduleGroup.destinationStation.name.uppercase().contains(query) &&
                                scheduleGroup.schedules.isNotEmpty()
                            } ?: 0
                        destinationGroup.station.id to matchCount
                    }
                    searchResults = results
                } else {
                    searchResults = emptyMap()
                }
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { pagerState.targetPage }
            .collect {
                destinationGroups.getOrNull(pagerState.targetPage)?.let {
                    onTabFocused(it.station.id)
                }
            }
    }

    BackHandler(searchQuery != null) {
        searchQuery = null
    }

    Scaffold(
        topBar = {
            val tab = tabs.getOrNull(pagerState.currentPage)
            val searchEnabled = tab?.searchEnabled

            SearchToolbar(
                titleContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icon),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        AppBarTitle("Comuline")
                    }
                },
                searchEnabled = searchEnabled ?: true,
                searchQuery = searchQuery,
                onChangeSearchQuery = { searchQuery = it },
                actions = {
                    AppBarActions(
                        actions = tab?.actions ?: listOf(
                            AppBar.Action(
                                title = "Add",
                                icon = Icons.Default.Add,
                                onClick = onClickAddStation,
                            ),
                        )
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        if (destinationGroups.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Default.Train,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "No pinned stations found\n Pin station to track station schedules",
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onClickAddStation,
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
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                ),
        ) {
            when {
                tabs.size <= 3 -> {
                    PrimaryTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier.zIndex(1f),
                    ) {
                        tabs.take(3).forEachIndexed { index, tab ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = { TabText(text = tab.title, badgeCount = tab.badgeNumber) },
                                unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                else -> {
                    Column {
                        ScrollableTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            modifier = Modifier.zIndex(1f),
                            divider = {},
                            edgePadding = 0.dp
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                    text = { TabText(text = tab.title, badgeCount = tab.badgeNumber) },
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                verticalAlignment = Alignment.Top,
            ) { page ->
                tabs[page].content(
                    PaddingValues(bottom = contentPadding.calculateBottomPadding()),
                    snackbarHostState,
                )
            }
        }
    }
}

@Composable
private fun mapTabContents(
    destinationGroups: List<DestinationGroup>,
    syncStates: Map<String, State<WorkInfo.State?>>,
    searchQuery: String?,
    searchResults: Map<String, Int>,
    onClickAddStation: () -> Unit,
    onClickStationDetail: (String, String) -> Unit,
): List<TabContent> {
    val tabs = destinationGroups.map { group ->
        val syncState = syncStates[group.station.id]?.value
        val badgeCount = searchResults[group.station.id]?.takeIf { it > 0 }
        TabContent(
            title = group.station.name,
            badgeNumber = badgeCount,
            searchEnabled = true,
            actions = listOf(
                AppBar.Action(
                    title = "Add",
                    icon = Icons.Default.Add,
                    onClick = onClickAddStation,
                ),
            ),
            content = { contentPadding, _ ->
                val schedules = group.scheduleGroup.collectAsState().value
                val query = searchQuery?.uppercase()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (schedules?.isNotEmpty() == true && schedules.flatMap { it.schedules }.isNotEmpty()) {
                        // Filter schedules based on search query
                        val filteredSchedules = if (!query.isNullOrEmpty()) {
                            schedules.filter { scheduleGroup ->
                                scheduleGroup.destinationStation.name.uppercase().contains(query)
                            }
                        } else {
                            schedules
                        }

                        val routes = filteredSchedules
                            .groupBy { it.schedules.firstOrNull()?.schedule?.line }
                            .mapNotNull {
                                if (it.key != null) {
                                    Pair(it.key!!, it.value)
                                } else null
                            }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(routes) { route ->
                                OutlinedCard {
                                    val density = LocalDensity.current
                                    val firstSchedule = route.second.first().schedules.first().schedule
                                    var height by remember { mutableStateOf(0.dp) }
                                    val color = firstSchedule.color.toColor()
                                    Row {
                                        Box(
                                            modifier = Modifier
                                                .height(height)
                                                .background(firstSchedule.color.toColor())
                                                .padding(start = 6.dp),
                                        )
                                        Column(
                                            modifier = Modifier
                                                .onSizeChanged { with(density) { height = it.height.toDp() } }
                                        ) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                modifier = Modifier.padding(start = 16.dp),
                                                text = firstSchedule.line,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isSystemInDarkTheme()) {
                                                    color.brighter(0.35f)
                                                } else color.darken(0.10f),
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            HorizontalDivider()
                                            route.second.forEachIndexed { index, item ->
                                                ScheduleItem(
                                                    index = index,
                                                    lastIndex = route.second.lastIndex,
                                                    scheduleGroup = item,
                                                    onClick = {
                                                        onClickStationDetail(
                                                            route.first,
                                                            item.schedules.first().schedule.trainId
                                                        )
                                                    }
                                                )
                                            }
                                            HorizontalDivider()
                                        }
                                    }

                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                modifier = Modifier.size(36.dp),
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = "No schedule found.\nCheck again tomorrow.",
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    if (schedules == null || syncState?.isFinished?.not() == true) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
        )
    }
    return tabs
}

@Composable
private fun ScheduleItem(
    index: Int,
    lastIndex: Int,
    scheduleGroup: DestinationGroup.ScheduleGroup,
    onClick: () -> Unit,
) {
    val station = scheduleGroup.destinationStation
    val schedules = scheduleGroup.schedules.ifEmpty { return }
    val firstSchedule = schedules.first().schedule
    val firstScheduleEta = schedules.first().eta
    val stops = schedules.first().stops

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(top = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = firstSchedule.departsAt.format(
                        DateTimeFormatter.ofPattern("HH:mm")
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = firstScheduleEta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            VerticalDivider()
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            imageVector = Icons.Default.Train,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = firstSchedule.trainId,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stops?.let { "$it stops" } ?: "Unknown",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
        if (index != lastIndex) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        if (index == lastIndex) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}