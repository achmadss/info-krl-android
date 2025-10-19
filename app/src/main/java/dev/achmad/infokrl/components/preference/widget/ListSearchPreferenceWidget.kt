package dev.achmad.infokrl.components.preference.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.achmad.infokrl.components.ScrollbarLazyColumn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ListSearchPreferenceWidget(
    value: T,
    enabled: Boolean,
    title: String,
    subtitle: String?,
    icon: ImageVector?,
    entries: () -> Map<out T, String>,
    onValueChange: (T) -> Unit,
) {
    val density = LocalDensity.current
    var searchBarHeight by remember { mutableStateOf(0.dp) }
    var isDialogShown by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchEntries by remember {
       derivedStateOf {
           entries().filter {
               if (searchQuery.isNotEmpty()) {
                   it.value.lowercase().contains(searchQuery.lowercase())
               } else true
           }
       }
    }

    TextPreferenceWidget(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onPreferenceClick = { if (enabled) isDialogShown = true },
    )

    if (isDialogShown) {
        searchQuery = ""
        AlertDialog(
            onDismissRequest = { isDialogShown = false },
            title = { Text(text = title) },
            text = {
                Box {
                    val state = rememberLazyListState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged {
                                searchBarHeight = with(density) {
                                    it.height.toDp()
                                }
                            },
                    ) {
                        SearchBar(
                            modifier = Modifier.offset(y = (-4).dp),
                            inputField = {
                                SearchBarDefaults.InputField(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onSearch = {},
                                    expanded = false,
                                    onExpandedChange = {},
                                    placeholder = { Text("Search") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null
                                        )
                                    }
                                )
                            },
                            shape = RectangleShape,
                            expanded = false,
                            onExpandedChange = {},
                            content = {},
                        )
                        HorizontalDivider()
                    }
                    if (searchEntries.isEmpty()) {
                        Text(
                            text = "No entries found",
                            style = MaterialTheme.typography.bodyLarge.merge(),
                            modifier = Modifier
                                .padding(top = searchBarHeight)
                                .align(Alignment.Center)
                                .padding(vertical = 48.dp),
                        )
                    } else {
                        ScrollbarLazyColumn(
                            modifier = Modifier
                                .padding(top = searchBarHeight)
                                .padding(top = 8.dp),
                            state = state,
                        ) {
                            searchEntries.forEach { current ->
                                val isSelected = value == current.key
                                item {
                                    DialogRow(
                                        label = current.value,
                                        isSelected = isSelected,
                                        onSelected = {
                                            onValueChange(current.key!!)
                                            isDialogShown = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.align(Alignment.TopCenter))
                    if (state.canScrollForward) {
                        HorizontalDivider(modifier = Modifier.align(Alignment.BottomCenter))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isDialogShown = false }) {
                    Text(text = "Cancel") // TODO copy
                }
            },
        )
    }
}

@Composable
private fun DialogRow(
    label: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .selectable(
                selected = isSelected,
                onClick = { if (!isSelected) onSelected() },
            )
            .fillMaxWidth()
            .minimumInteractiveComponentSize(),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.merge(),
            modifier = Modifier.padding(start = 24.dp),
        )
    }
}
