package dev.achmad.infokrl.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabNavigator
import dev.achmad.infokrl.screens.home.more.MoreTab
import dev.achmad.infokrl.screens.home.schedules.SchedulesTab
import dev.achmad.infokrl.screens.home.stations.StationsTab
import dev.achmad.infokrl.screens.home.trip.TripTab
import soup.compose.material.motion.animation.materialFadeThroughIn
import soup.compose.material.motion.animation.materialFadeThroughOut

object HomeScreen: Screen {
    private fun readResolve(): Any = HomeScreen

    @Suppress("ConstPropertyName")
    private const val TabFadeDuration = 200

    @Suppress("ConstPropertyName")
    private const val TabNavigatorKey = "HomeTabs"

    private val TABS = listOf(
        SchedulesTab,
        StationsTab,
        TripTab,
        MoreTab
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TabNavigator(
            tab = SchedulesTab,
            key = TabNavigatorKey,
        ) { tabNavigator ->

            BackHandler(tabNavigator.current != SchedulesTab) {
                tabNavigator.current = SchedulesTab
            }

            CompositionLocalProvider(LocalNavigator provides navigator) {
                Scaffold(
                    bottomBar = {
                        HomeNavigationBar(tabNavigator)
                    }
                ) { contentPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding)
                            .consumeWindowInsets(contentPadding)
                    ) {
                        AnimatedContent(
                            targetState = tabNavigator.current,
                            transitionSpec = {
                                materialFadeThroughIn(
                                    initialScale = 1f,
                                    durationMillis = TabFadeDuration
                                ) togetherWith materialFadeThroughOut(TabFadeDuration)
                            },
                            label = "tabContent",
                        ) {
                            tabNavigator.saveableState(key = "currentTab", it) {
                                it.Content()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun HomeNavigationBar(tabNavigator: TabNavigator) {
        NavigationBar {
            TABS.fastForEach { tab ->
                val selected = tabNavigator.current::class == tab::class
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            tabNavigator.current = tab
                        }
                    },
                    icon = {
                        tab.options.icon?.let { icon ->
                            Icon(
                                painter = icon,
                                contentDescription = null
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.options.title,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                )
            }
        }
    }
}