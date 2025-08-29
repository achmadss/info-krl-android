package dev.achmad.comuline.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> SingleSelectFilterChipGroup(
    options: List<Pair<T, String>>,
    selectedOption: Pair<T, String>?,
    onSelectionChanged: (Pair<T, String>) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Column(modifier = modifier) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = selectedOption == option
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onSelectionChanged(option)
                    },
                    label = { Text(option.second) },
                    colors = FilterChipDefaults.filterChipColors(),
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }
        }
    }
}


@Composable
fun <T> MultiSelectFilterChipGroup(
    options: List<Pair<T, String>>,
    onSelectionChanged: (List<Pair<T, String>>) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    selectedOptions: List<Pair<T, String>>,
) {
    Column(
        modifier = modifier,
    ) {
        title?.let {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = selectedOptions.contains(option)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newSelection = if (isSelected) {
                            selectedOptions - option
                        } else {
                            selectedOptions + option
                        }
                        onSelectionChanged(newSelection)
                    },
                    label = { Text(option.second) },
                    colors = FilterChipDefaults.filterChipColors().copy(),
                    leadingIcon = {
                        Icon(
                            imageVector = when {
                                isSelected -> Icons.Filled.Done
                                else -> Icons.Filled.Close
                            },
                            tint = when {
                                isSelected -> LocalContentColor.current
                                else -> MaterialTheme.colorScheme.error
                            },
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
            }
        }
    }
}
