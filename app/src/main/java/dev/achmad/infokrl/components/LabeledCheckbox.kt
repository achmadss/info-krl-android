package dev.achmad.infokrl.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

data class LabeledCheckboxData(
    val label: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val modifier: Modifier = Modifier,
    val enabled: Boolean = true,
)

@Composable
fun LabeledCheckbox(
    data: LabeledCheckboxData,
) {
    LabeledCheckbox(
        label = data.label,
        checked = data.checked,
        onCheckedChange = data.onCheckedChange,
        modifier = data.modifier,
        enabled = data.enabled,
    )
}

@Composable
fun LabeledCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(
                role = Role.Checkbox,
                onClick = {
                    if (enabled) {
                        onCheckedChange(!checked)
                    }
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            modifier = Modifier.padding(start = 12.dp),
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
        )

        Text(text = label)
    }
}

@Composable
fun LabeledCheckboxGroup(
    items: List<LabeledCheckboxData>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    title: String? = null,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        title?.let {
            item {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        items(items) {
            LabeledCheckbox(it)
        }
    }
}