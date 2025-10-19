package dev.achmad.infokrl.screens.settings.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.achmad.infokrl.base.ApplicationPreference

@Composable
fun themeOptions(): Map<ApplicationPreference.Themes, String> {
    return ApplicationPreference.Themes.entries.associateWith {
        stringResource(it.stringRes)
    }
}
