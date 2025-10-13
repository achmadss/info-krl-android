package dev.achmad.comuline.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import dev.achmad.comuline.BuildConfig
import dev.achmad.comuline.R
import dev.achmad.comuline.components.AppBar
import dev.achmad.comuline.components.AppBarActions
import dev.achmad.comuline.components.AppBarTitle
import dev.achmad.comuline.components.SearchToolbar
import dev.achmad.comuline.components.TabText
import dev.achmad.comuline.screens.home.station_detail.StationDetailScreen
import dev.achmad.comuline.screens.settings.SettingsScreen
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
        val filterFutureSchedulesOnly by screenModel.filterFutureSchedulesOnly.collectAsState()

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
            filterFutureSchedulesOnly = filterFutureSchedulesOnly,
            onTabFocused = { stationId ->
                screenModel.onTabFocused(stationId)
            },
            onClickAddStation = {
                navigator.push(StationsScreen)
            },
            onClickStationDetail = { originStationId, destinationStationId, scheduleId ->
                navigator.push(
                    StationDetailScreen(
                        originStationId = originStationId,
                        destinationStationId = destinationStationId,
                        scheduleId = scheduleId
                    )
                )
            },
            onManualSync = {
                screenModel.fetchSchedules(true)
            },
            onRefreshStation = { stationId ->
                screenModel.fetchScheduleForStation(stationId, true)
            },
            onToggleFilterFutureSchedules = {
                screenModel.toggleFilterFutureSchedules()
            },
            onNavigateToSettings = {
                navigator.push(SettingsScreen)
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
    filterFutureSchedulesOnly: Boolean,
    onTabFocused: (String) -> Unit,
    onClickAddStation: () -> Unit,
    onClickStationDetail: (String, String, String) -> Unit,
    onManualSync: () -> Unit,
    onRefreshStation: (String) -> Unit,
    onToggleFilterFutureSchedules: () -> Unit,
    onNavigateToSettings: () -> Unit,
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

    // Cache sync states to prevent recreation on every recomposition
//    val syncStates = remember(destinationGroups) {
//        destinationGroups.associate { group ->
//            group.station.id to SyncScheduleJob.subscribeState(
//                context = applicationContext,
//                scope = syncScope,
//                stationId = group.station.id
//            )
//        }
//    }.mapValues { (_, flow) -> flow.collectAsState() }

    val tabs = mapTabContents(
        destinationGroups = destinationGroups,
        syncStates = syncStates,
        searchQuery = searchQuery,
        searchResults = searchResults,
        onClickStationDetail = onClickStationDetail,
        onRefreshStation = onRefreshStation
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
                        actions = if (searchQuery == null) {
                            listOf(
                                AppBar.Action(
                                    title = "Add",
                                    icon = Icons.Default.Add,
                                    onClick = onClickAddStation,
                                ),
                                AppBar.OverflowAction(
                                    title = "Sync All",
                                    icon = Icons.Default.Refresh,
                                    onClick = { onManualSync() },
                                ),
                                AppBar.OverflowAction(
                                    title = "Settings",
                                    icon = Icons.Outlined.Settings,
                                    onClick = { onNavigateToSettings() },
                                ),
                                AppBar.OverflowAction(
                                    title = "About",
                                    icon = Icons.Outlined.Info,
                                    onClick = {
                                        // TODO
                                    },
                                ),
                            ) + if (BuildConfig.DEBUG) {
                                listOf(
                                    AppBar.OverflowAction(
                                        title = "----- DEBUG -----",
                                        enabled = false,
                                        onClick = {}
                                    ),
                                    AppBar.OverflowAction(
                                        title = if (filterFutureSchedulesOnly) "Show All Schedules" else "Hide Past Schedules",
                                        icon = Icons.Default.EventBusy,
                                        onClick = onToggleFilterFutureSchedules,
                                    )
                                )
                            } else emptyList()
                        } else emptyList()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun mapTabContents(
    destinationGroups: List<DestinationGroup>,
    syncStates: Map<String, State<WorkInfo.State?>>,
    searchQuery: String?,
    searchResults: Map<String, Int>,
    onClickStationDetail: (String, String, String) -> Unit,
    onRefreshStation: (String) -> Unit,
): List<TabContent> {
    return destinationGroups.map { group ->
        val syncState = syncStates[group.station.id]?.value
        val badgeCount = searchResults[group.station.id]?.takeIf { it > 0 }
        TabContent(
            title = group.station.name,
            badgeNumber = badgeCount,
            searchEnabled = true,
            actions = emptyList(),
            content = { contentPadding, _ ->
                val schedules = group.scheduleGroup.collectAsState().value
                val query = searchQuery?.uppercase()
                val isRefreshing = syncState?.isFinished?.not() == true

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { onRefreshStation(group.station.id) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        if (schedules?.isNotEmpty() == true && schedules.flatMap { it.schedules }.isNotEmpty()) {
                            val filteredSchedules = when {
                                !query.isNullOrEmpty() -> schedules.filter { scheduleGroup ->
                                    scheduleGroup.destinationStation.name.uppercase().contains(query)
                                }
                                else -> schedules
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(contentPadding),
                            ) {
                                itemsIndexed(
                                    items = filteredSchedules,
                                    key = { _, item -> item.destinationStation.id }
                                ) { index, schedule ->
                                    ScheduleItem(index, schedules.lastIndex, schedule) {
                                        onClickStationDetail(
                                            group.station.id,
                                            schedule.destinationStation.id,
                                            schedule.schedules.first().schedule.id
                                        )
                                    }
                                }
                                item {
                                    HorizontalDivider()
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
                        if (schedules == null || isRefreshing) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun ScheduleItem(
    index: Int,
    lastIndex: Int,
    scheduleGroup: DestinationGroup.ScheduleGroup,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val station = scheduleGroup.destinationStation
    val schedules = scheduleGroup.schedules.ifEmpty { return }
    val firstSchedule = schedules.first().schedule
    val firstScheduleEta = schedules.first().eta
    val color = firstSchedule.color.toColor()
    var height by remember { mutableStateOf(0.dp) }
    val stops = schedules.first().stops

    Row(
        modifier = Modifier
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .height(height)
                .background(
                    color = color,
                )
                .padding(start = 6.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { with(density) { height = it.height.toDp() } }
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp),
        ) {
            Text(
                text = firstSchedule.line,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSystemInDarkTheme()) {
                    color.brighter(.35f)
                } else color.darken(.15f),
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Directions to",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                Text(
                    text = "Departs at",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                )
                Text(
                    text = firstSchedule.departsAt.format(
                        DateTimeFormatter.ofPattern("HH:mm")
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(12.dp),
                        imageVector = Icons.Default.Train,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = firstSchedule.trainId,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        modifier = Modifier.size(12.dp),
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (stops != null) "$stops stops" else "Unknown",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                Text(
                    text = firstScheduleEta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Next departures",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    val previewSchedules =
                        if (schedules.size <= 4) schedules
                        else schedules.take(5).drop(1)

                    previewSchedules.map { schedule ->
                        Column(
                            modifier = Modifier.weight(0.25f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = schedule.schedule.departsAt.format(
                                    DateTimeFormatter.ofPattern("HH:mm")
                                ),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                            Text(
                                text = schedule.eta,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                )
            }
            if (index != lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (index == lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

}