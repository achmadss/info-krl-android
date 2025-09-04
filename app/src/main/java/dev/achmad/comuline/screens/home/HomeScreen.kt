package dev.achmad.comuline.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import dev.achmad.comuline.screens.stations.StationsScreen
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
        val applicationContext = LocalContext.current.applicationContext
        val screenModel = rememberScreenModel { HomeScreenModel() }
        val destinationGroups by screenModel.destinationGroups.collectAsState()

        LaunchedEffect(Unit) {
            screenModel.startAutoRefresh(applicationContext)
        }

        HomeScreen(
            syncScope = screenModel.screenModelScope,
            destinationGroups = destinationGroups,
            onClickAddStation = {
                navigator.push(StationsScreen)
            },
            onTabFocused = { stationId ->
                screenModel.onTabFocused(applicationContext, stationId)
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun HomeScreen(
    syncScope: CoroutineScope,
    destinationGroups: List<DestinationGroup>,
    onClickAddStation: () -> Unit,
    onTabFocused: (String) -> Unit,
) {
    val applicationContext = LocalContext.current.applicationContext
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()

    var searchQuery by rememberSaveable { mutableStateOf<String?>(null) }

    val syncStates = destinationGroups.associate { group ->
        group.station.id to remember(group.station.id) {
            SyncScheduleJob.subscribeState(
                context = applicationContext,
                scope = syncScope,
                stationId = group.station.id
            )
        }.collectAsState()
    }
    val tabs = mapTabContents(destinationGroups, syncStates, onClickAddStation)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState { tabs.size }

    LaunchedEffect(destinationGroups) {
        snapshotFlow { searchQuery }
            .debounce(300)
            .collect { query ->
                if (!query.isNullOrEmpty()) {
                    destinationGroups.firstOrNull { it.station.name.contains(query, ignoreCase = true) }
                        ?.let { pagerState.animateScrollToPage(destinationGroups.indexOf(it)) }
                }
            }
    }

    LaunchedEffect(destinationGroups) {
        snapshotFlow { pagerState.targetPage }
            .collect {
                destinationGroups.getOrNull(pagerState.targetPage)?.let {
                    onTabFocused(it.station.id)
                }
            }
    }

    LaunchedEffect(destinationGroups) {
        if (destinationGroups.isNotEmpty()) {
            when(lifecycleState) {
                Lifecycle.State.CREATED, Lifecycle.State.RESUMED -> {
                    destinationGroups.getOrNull(pagerState.currentPage)?.station?.id?.let {
                        SyncScheduleJob.start(
                            context = applicationContext,
                            stationId = it,
                        )
                    }
                }
                else -> Unit
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
                modifier = Modifier.fillMaxSize().padding(contentPadding),
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
    onClickAddStation: () -> Unit,
): List<TabContent> {
    val tabs = destinationGroups.map { group ->
        val syncState = syncStates[group.station.id]?.value
        TabContent(
            title = group.station.name,
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (schedules?.isNotEmpty() == true) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding),
                        ) {
                            itemsIndexed(
                                items = schedules,
                                key = { _, item -> item.destinationStation.id }
                            ) { index, schedule ->
                                ScheduleItem(index, schedules.lastIndex, schedule)
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
) {
    val density = LocalDensity.current
    val station = scheduleGroup.destinationStation
    val schedules = scheduleGroup.schedules.ifEmpty { return }
    val firstSchedule = schedules.first().first
    val firstScheduleEta = schedules.first().second
    val color = firstSchedule.color.toColor()
    var height by remember { mutableStateOf(0.dp) }

    Row(
        modifier = Modifier,
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
                color = color,
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
                Text(
                    text = firstSchedule.trainId,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
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
                                text = schedule.first.departsAt.format(
                                    DateTimeFormatter.ofPattern("HH:mm")
                                ),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                            Text(
                                text = schedule.second,
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