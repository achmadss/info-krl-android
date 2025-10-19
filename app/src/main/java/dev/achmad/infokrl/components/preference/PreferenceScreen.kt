package dev.achmad.infokrl.components.preference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import dev.achmad.infokrl.components.AppBar
import dev.achmad.infokrl.components.ScrollbarLazyColumn
import dev.achmad.infokrl.components.preference.widget.PreferenceGroupHeader
import dev.achmad.infokrl.util.onClickInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceScreen(
    title: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    shadowElevation: Dp = 0.dp,
    actions: @Composable RowScope.() -> Unit = {},
    onBackPressed: (() -> Unit)? = null,
    itemsProvider: @Composable () -> List<Preference>,
    topBarScrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        rememberTopAppBarState(),
    ),
    bottomBar: @Composable () -> Unit = {},
) {
    val items = itemsProvider()
    Box {
        Scaffold(
            topBar = {
                Surface(
                    shadowElevation = shadowElevation
                ) {
                    AppBar(
                        title = title,
                        navigateUp = onBackPressed,
                        actions = actions,
                        scrollBehavior = topBarScrollBehavior,
                    )
                }
            },
            bottomBar = { bottomBar() },
            content = { contentPadding ->
                val lazyListState = rememberLazyListState()
                ScrollbarLazyColumn(
                    modifier = modifier,
                    state = lazyListState,
                    contentPadding = contentPadding,
                ) {
                    items.fastForEachIndexed { i, preference ->
                        when (preference) {
                            // Create Preference Group
                            is Preference.PreferenceGroup -> {
                                item {
                                    Column {
                                        PreferenceGroupHeader(
                                            title = preference.title,
                                            visible = preference.visible
                                        )
                                    }
                                }
                                if (!preference.visible) return@fastForEachIndexed
                                items(preference.preferenceItems) { item ->
                                    PreferenceItem(
                                        item = item,
                                        highlightKey = null,
                                    )
                                }
                                item {
                                    if (i < items.lastIndex) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            }

                            // Create Preference Item
                            is Preference.PreferenceItem<*> -> item {
                                PreferenceItem(
                                    item = preference,
                                    highlightKey = null,
                                )
                            }
                        }
                    }
                }
            },
        )

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .then(
                        Modifier.onClickInput(
                            pass = PointerEventPass.Initial,
                            ripple = false,
                            onUp = {
                                // do nothing
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

    }

}

private fun List<Preference>.findHighlightedIndex(highlightKey: String): Int {
    return flatMap {
        if (it is Preference.PreferenceGroup) {
            buildList<String?> {
                add(null) // Header
                addAll(it.preferenceItems.map { groupItem -> groupItem.title })
                add(null) // Spacer
            }
        } else {
            listOf(it.title)
        }
    }.indexOfFirst { it == highlightKey }
}
