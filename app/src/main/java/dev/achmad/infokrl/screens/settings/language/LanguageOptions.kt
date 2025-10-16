package dev.achmad.infokrl.screens.settings.language

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.achmad.infokrl.R

@Composable
fun localeOptions(): Map<String, String> {
    return mapOf(
        R.string.language_english to "en",
        R.string.language_indonesian to "id",
    ).mapKeys { stringResource(it.key) }
}