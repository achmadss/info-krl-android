package dev.achmad.comuline.screens.settings.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.achmad.comuline.base.ApplicationPreference

@Composable
fun themeOptions(): Map<ApplicationPreference.Themes, String> {
    return ApplicationPreference.Themes.entries.associateWith {
        stringResource(it.stringRes)
    }
}
