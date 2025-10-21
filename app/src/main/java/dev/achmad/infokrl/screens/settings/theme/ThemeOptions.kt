package dev.achmad.infokrl.screens.settings.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.achmad.infokrl.R
import dev.achmad.domain.theme.Themes

@Composable
fun themeOptions(): Map<Themes, String> {
    return Themes.entries.associateWith {
        when(it) {
            Themes.LIGHT -> stringResource(R.string.theme_light)
            Themes.DARK -> stringResource(R.string.theme_dark)
            Themes.SYSTEM -> stringResource(R.string.theme_system)
        }
    }
}
