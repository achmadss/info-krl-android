package dev.achmad.infokrl.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FromToSelector(
    fromLabel: String,
    fromValue: String,
    onFromValueChanged: () -> Unit,
    toLabel: String,
    toValue: String,
    onToValueChanged: () -> Unit,
    onClickSwapButton: () -> Unit = {},
    onBothFilledChanged: ((fromValue: String, toValue: String) -> Unit)? = null,
    onClickOverrideButton: (() -> Unit)? = null
) {
    // Trigger callback when both fields are filled and whenever either changes (only if no override button is provided)
    LaunchedEffect(fromValue, toValue) {
        if (onClickOverrideButton == null && fromValue.isNotBlank() && toValue.isNotBlank()) {
            onBothFilledChanged?.invoke(fromValue, toValue)
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            StationTextField(
                label = fromLabel,
                value = fromValue,
                onClick = onFromValueChanged
            )

            Spacer(modifier = Modifier.height(12.dp))

            StationTextField(
                label = toLabel,
                value = toValue,
                onClick = onToValueChanged
            )
        }

        // Swap button positioned between the two text fields
        FilledIconButton(
            onClick = onClickSwapButton,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-8).dp)
                .size(40.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Filled.SwapVert,
                contentDescription = "Swap stations",
                modifier = Modifier.size(24.dp)
            )
        }

        if (onClickOverrideButton != null) {
            FilledIconButton(
                onClick = onClickOverrideButton,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-8).dp, y = 8.dp)
                    .size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Override stations",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}