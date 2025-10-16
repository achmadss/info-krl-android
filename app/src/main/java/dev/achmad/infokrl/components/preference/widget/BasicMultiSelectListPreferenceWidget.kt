package dev.achmad.infokrl.components.preference.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import dev.achmad.infokrl.components.LabeledCheckbox
import dev.achmad.infokrl.components.ScrollbarLazyColumn
import dev.achmad.infokrl.components.preference.Preference

@Composable
fun BasicMultiSelectListPreferenceWidget(
    preference: Preference.PreferenceItem.BasicMultiSelectListPreference,
    enabled: Boolean,
    onValuesChange: (List<String>) -> Unit,
) {
    var isDialogShown by remember { mutableStateOf(false) }

    TextPreferenceWidget(
        title = preference.title,
        subtitle = preference.subtitleProvider(preference.values, preference.entries),
        icon = preference.icon,
        onPreferenceClick = { if (enabled) isDialogShown = true },
    )

    if (isDialogShown) {
        val selected = remember {
            preference.entries.keys
                .filter { preference.values.contains(it) }
                .toMutableStateList()
        }
        AlertDialog(
            onDismissRequest = { isDialogShown = false },
            title = { Text(text = preference.title) },
            text = {
                Box {
                    val state = rememberLazyListState()
                    ScrollbarLazyColumn(state = state) {
                        preference.entries.forEach { current ->
                            item {
                                val isSelected = selected.contains(current.key)
                                LabeledCheckbox(
                                    label = current.value,
                                    checked = isSelected,
                                    onCheckedChange = {
                                        if (it) {
                                            selected.add(current.key)
                                        } else {
                                            selected.remove(current.key)
                                        }
                                    },
                                )
                            }
                        }
                    }
                    if (state.canScrollBackward) HorizontalDivider(modifier = Modifier.align(Alignment.TopCenter))
                    if (state.canScrollForward) HorizontalDivider(modifier = Modifier.align(Alignment.BottomCenter))
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = true,
            ),
            confirmButton = {
                TextButton(
                    onClick = {
                        onValuesChange(selected)
                        isDialogShown = false
                    },
                ) {
                    Text(text = "OK") // TODO copy
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogShown = false }) {
                    Text(text = "Cancel") // TODO copy
                }
            },
        )
    }
}