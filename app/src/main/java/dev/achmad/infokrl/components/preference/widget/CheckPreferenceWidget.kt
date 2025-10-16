package dev.achmad.infokrl.components.preference.widget

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CheckPreferenceWidget(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: CharSequence? = null,
    value: String,
    icon: ImageVector? = null,
    checked: Boolean = false,
    onClick: (String) -> Unit,
) {
    TextPreferenceWidget(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        icon = icon,
        widget = if (checked) {
            {
                Icon(
                    modifier = Modifier.padding(start = TrailingWidgetBuffer),
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        onPreferenceClick = { onClick(value) }
    )
}