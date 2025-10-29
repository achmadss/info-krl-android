package dev.achmad.infokrl.screens.home.schedules

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.preference.ApplicationPreference
import dev.achmad.domain.schedule.interactor.SyncSchedule
import dev.achmad.infokrl.R
import dev.achmad.infokrl.components.AppBar
import dev.achmad.infokrl.components.AppBarActions
import dev.achmad.infokrl.components.AppBarTitle
import dev.achmad.infokrl.components.SearchToolbar
import dev.achmad.infokrl.components.TabText
import dev.achmad.infokrl.screens.home.stations.StationsTab
import dev.achmad.infokrl.screens.schedules.SchedulesScreen
import dev.achmad.infokrl.theme.LocalColorScheme
import dev.achmad.infokrl.theme.darkTheme
import dev.achmad.infokrl.util.bottomBorder
import dev.achmad.infokrl.util.brighter
import dev.achmad.infokrl.util.collectAsState
import dev.achmad.infokrl.util.darken
import dev.achmad.infokrl.util.timeFormatter
import dev.achmad.infokrl.util.toColor
import dev.achmad.infokrl.util.topBorder
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

data class TabContent(
    val title: String,
    val badgeNumber: Int? = null,
    val searchEnabled: Boolean = false,
    val actions: List<AppBar.AppBarAction> = listOf(),
    val content: @Composable (contentPadding: PaddingValues, snackbarHostState: SnackbarHostState) -> Unit,
)

object SchedulesTab: Tab {
    private fun readResolve(): Any = SchedulesTab

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            return TabOptions(
                index = 1u,
                title = stringResource(R.string.schedules),
                icon = rememberVectorPainter(
                    when {
                        isSelected -> Icons.Filled.CalendarMonth
                        else -> Icons.Outlined.CalendarMonth
                    }
                ),
            )
        }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val tabNavigator = LocalTabNavigator.current
        val screenModel = rememberScreenModel { SchedulesTabScreenModel() }
        val destinationGroups by screenModel.departureGroups.collectAsState()
        val focusedStationId by screenModel.focusedStationId.collectAsState()
        val syncScheduleResult by screenModel.syncScheduleResult.collectAsState()
        val applicationPreference by injectLazy<ApplicationPreference>()
        val is24Hour by applicationPreference.is24HourFormat().collectAsState()

        SchedulesTab(
            departureGroups = destinationGroups,
            focusedStationId = focusedStationId,
            isRefreshing = syncScheduleResult is SyncSchedule.Result.Loading,
            is24Hour = is24Hour,
            onTabFocused = { stationId ->
                screenModel.onTabFocused(stationId)
            },
            onClickAddStation = {
                tabNavigator.current = StationsTab
            },
            onClickStationDetail = { originStationId, destinationStationId, line, scheduleId ->
                navigator.push(
                    SchedulesScreen(
                        originStationId = originStationId,
                        destinationStationId = destinationStationId,
                        line = line,
                        scheduleId = scheduleId
                    )
                )
            },
            onRefreshAll = {
                screenModel.refreshAllStations()
            },
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun SchedulesTab(
    departureGroups: List<DepartureGroup>,
    focusedStationId: String?,
    isRefreshing: Boolean,
    onTabFocused: (String) -> Unit,
    onClickAddStation: () -> Unit,
    onClickStationDetail: (String, String, String, String) -> Unit,
    onRefreshAll: () -> Unit,
    is24Hour: Boolean,
) {
    var searchQuery by rememberSaveable { mutableStateOf<String?>(null) }
    var searchResults by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    val tabs = mapTabContents(
        departureGroups = departureGroups,
        isRefreshing = isRefreshing,
        searchQuery = searchQuery,
        searchResults = searchResults,
        is24Hour = is24Hour,
        onClickStationDetail = onClickStationDetail,
        onRefreshAll = onRefreshAll
    )
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val initialPage = remember(departureGroups, focusedStationId) {
        if (focusedStationId != null) {
            departureGroups
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

    LaunchedEffect(searchQuery, departureGroups) {
        snapshotFlow { searchQuery?.uppercase() }
            .debounce(300)
            .collect { query ->
                if (!query.isNullOrEmpty()) {
                    val results = departureGroups.associate { destinationGroup ->
                        val matchCount = destinationGroup.scheduleGroup.value
                            ?.count { scheduleGroup ->
                                // Only count if line or any destination matches AND has upcoming schedules
                                (scheduleGroup.line.uppercase().contains(query) ||
                                scheduleGroup.destinationGroups.any { it.destinationStation.name.uppercase().contains(query) }) &&
                                scheduleGroup.destinationGroups.any { it.schedules.isNotEmpty() }
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
                departureGroups.getOrNull(pagerState.targetPage)?.let {
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
                    AppBarTitle(stringResource(R.string.schedules))
                },
                searchEnabled = searchEnabled ?: true,
                searchQuery = searchQuery,
                onChangeSearchQuery = { searchQuery = it },
                actions = {
                    AppBarActions(
                        actions = if (searchQuery == null) {
                            listOf(
                                AppBar.Action(
                                    title = "Filter",
                                    icon = Icons.Default.FilterList,
                                    onClick = {
                                        // TODO show bottom sheet to filter/sort
                                    },
                                ),
                            )
                        } else emptyList()
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        if (departureGroups.isEmpty()) {
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
                    text = stringResource(R.string.no_pinned_stations),
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
                            text = stringResource(R.string.action_add_station)
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
    departureGroups: List<DepartureGroup>,
    isRefreshing: Boolean,
    searchQuery: String?,
    searchResults: Map<String, Int>,
    is24Hour: Boolean,
    onClickStationDetail: (String, String, String, String) -> Unit,
    onRefreshAll: () -> Unit,
): List<TabContent> {
    return departureGroups.map { group ->
        val badgeCount = searchResults[group.station.id]?.takeIf { it > 0 }
        TabContent(
            title = group.station.name,
            badgeNumber = badgeCount,
            searchEnabled = true,
            actions = emptyList(),
            content = { contentPadding, _ ->
                val schedules = group.scheduleGroup.collectAsState().value
                val query = searchQuery?.uppercase()

                PullToRefreshBox(
                    isRefreshing = schedules == null || isRefreshing,
                    onRefresh = onRefreshAll,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        val allDestinationGroups = schedules?.flatMap { scheduleGroup ->
                            scheduleGroup.destinationGroups
                        } ?: emptyList()
                        
                        if (schedules?.isNotEmpty() == true && allDestinationGroups.isNotEmpty()) {
                            val filteredSchedules = when {
                                !query.isNullOrEmpty() -> schedules.filter { scheduleGroup ->
                                    scheduleGroup.line.uppercase().contains(query) ||
                                    scheduleGroup.destinationGroups.any { it.destinationStation.name.uppercase().contains(query) }
                                }
                                else -> schedules
                            }
                            
                            if (filteredSchedules.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(contentPadding),
                                ) {
                                    itemsIndexed(
                                        items = filteredSchedules,
                                        key = { _, item -> item.line }
                                    ) { index, scheduleGroup ->
                                        LineAccordion(
                                            index = index,
                                            scheduleGroup = scheduleGroup,
                                            is24Hour = is24Hour,
                                            onClickStationDetail = onClickStationDetail,
                                            originStationId = group.station.id
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
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
                                        text = stringResource(R.string.no_schedule_found_for_query, searchQuery ?: ""),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
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
                                    text = stringResource(R.string.no_schedule_found),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun LineAccordion(
    index: Int,
    scheduleGroup: DepartureGroup.ScheduleGroup,
    is24Hour: Boolean,
    onClickStationDetail: (originStationId: String, destinationStationId: String, line: String, scheduleId: String) -> Unit,
    originStationId: String
) {
    var expanded by rememberSaveable(key = scheduleGroup.line) { mutableStateOf(true) }
    val colorScheme = LocalColorScheme.current
    val firstSchedule = scheduleGroup.destinationGroups.firstOrNull()?.schedules?.firstOrNull()?.schedule
    val color = (scheduleGroup.color ?: firstSchedule?.color)?.toColor()
        ?: MaterialTheme.colorScheme.primary
    
    val degrees by animateFloatAsState(
        targetValue = if (expanded) -90f else 90f,
        label = "chevron_rotation"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Accordion Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (index > 0) Modifier
                    else Modifier.topBorder()
                )
                .bottomBorder()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(56.dp)
                        .background(color)
                )
                Text(
                    text = scheduleGroup.line,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (colorScheme == darkTheme) {
                        color.brighter(.35f)
                    } else color,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .rotate(degrees),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Accordion Content with Animation
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = IntSize.VisibilityThreshold
                )
            ),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
            ) {
                scheduleGroup.destinationGroups.forEachIndexed { index, destinationGroup ->
                    NewScheduleItem(
                        index = index,
                        lastIndex = scheduleGroup.destinationGroups.lastIndex,
                        scheduleGroup = scheduleGroup,
                        destinationGroup = destinationGroup,
                        is24Hour = is24Hour
                    ) {
                        onClickStationDetail(
                            originStationId,
                            destinationGroup.destinationStation.id,
                            scheduleGroup.line,
                            destinationGroup.schedules.first().schedule.id
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewScheduleItem(
    index: Int,
    lastIndex: Int,
    scheduleGroup: DepartureGroup.ScheduleGroup,
    destinationGroup: DepartureGroup.ScheduleGroup.DestinationGroup,
    is24Hour: Boolean,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val schedules = destinationGroup.schedules.ifEmpty { return }
    val firstScheduleItem = schedules.first()
    val firstSchedule = firstScheduleItem.schedule
    val destinationStation = destinationGroup.destinationStation
    val nextSchedule = schedules.getOrNull(1)?.schedule
    val color = (scheduleGroup.color ?: firstSchedule.color).toColor()
    var height by remember { mutableStateOf(0.dp) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                .padding(top = 12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.directions_to),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = destinationStation.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                    )
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
                    }
                }
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = stringResource(R.string.departs_at),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = firstSchedule.departsAt.format(
                            timeFormatter(is24Hour)
                        ),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                    )
                    nextSchedule?.departsAt?.let { departsAt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                modifier = Modifier.size(12.dp).offset(x = -2.dp, y = -1.dp),
                                imageVector = Icons.Default.SubdirectoryArrowRight,
                                tint = MaterialTheme.colorScheme.outline,
                                contentDescription = null,
                            )
                            Text(
                                text = departsAt.format(timeFormatter(is24Hour)),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }

                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun ScheduleItem(
    index: Int,
    lastIndex: Int,
    scheduleGroup: DepartureGroup.ScheduleGroup,
    destinationGroup: DepartureGroup.ScheduleGroup.DestinationGroup,
    is24Hour: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = LocalColorScheme.current
    val density = LocalDensity.current
    val schedules = destinationGroup.schedules.ifEmpty { return }
    val firstScheduleItem = schedules.first()
    val firstSchedule = firstScheduleItem.schedule
    val destinationStation = destinationGroup.destinationStation
    val firstScheduleEta = firstScheduleItem.eta
    val color = (scheduleGroup.color ?: firstSchedule.color).toColor()
    var height by remember { mutableStateOf(0.dp) }

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
                text = scheduleGroup.line,
                style = MaterialTheme.typography.labelMedium,
                color = if (colorScheme == darkTheme) {
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
                    text = stringResource(R.string.directions_to),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                Text(
                    text = stringResource(R.string.departs_at),
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
                    text = destinationStation.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                )
                Text(
                    text = firstSchedule.departsAt.format(
                        timeFormatter(is24Hour)
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
                }
                Text(
                    text = firstScheduleEta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.next_departures),
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
                                    timeFormatter(is24Hour)
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
