package dev.achmad.infokrl.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.achmad.domain.station.model.Station
import dev.achmad.infokrl.R

@Composable
fun StationListItem(
    station: Station,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTogglePin: (() -> Unit)? = null,
    isDragging: Boolean = false,
    showDragHandle: Boolean = false,
    enabled: Boolean = true
) {
    val icon = if (station.favorite) R.drawable.push_pin else R.drawable.push_pin_outline
    val backgroundColor = if (isDragging) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    // Use Surface wrapper only when needed (dragging or has advanced features)
    val useAdvancedLayout = onTogglePin != null || showDragHandle || isDragging

    if (useAdvancedLayout) {
        Surface(
            modifier = modifier,
            color = backgroundColor,
            shadowElevation = if (isDragging) 4.dp else 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled, onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showDragHandle) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = stringResource(R.string.content_desc_drag_handle),
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(
                    modifier = Modifier.weight(1f),
                    text = station.name,
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                if (onTogglePin != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = onTogglePin,
                        enabled = enabled
                    ) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    } else {
        // Simple default layout - matches original StationListItem
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = station.name,
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}