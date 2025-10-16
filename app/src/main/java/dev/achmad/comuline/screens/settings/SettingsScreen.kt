package dev.achmad.comuline.screens.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.comuline.R
import dev.achmad.comuline.base.ApplicationPreference
import dev.achmad.comuline.components.preference.Preference
import dev.achmad.comuline.components.preference.PreferenceScreen
import dev.achmad.comuline.screens.settings.language.SettingsLanguageScreen
import dev.achmad.comuline.screens.settings.language.localeOptions
import dev.achmad.core.di.util.injectLazy
import dev.achmad.core.preference.toggle

object SettingsScreen : Screen {
    private fun readResolve(): Any = SettingsScreen

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val appPreference by injectLazy<ApplicationPreference>()

        PreferenceScreen(
            title = stringResource(R.string.settings),
            shadowElevation = 4.dp,
            onBackPressed = {
                navigator.pop()
            },
            itemsProvider = {
                listOf(
                    appearanceGroup(appPreference, navigator),
                    aboutGroup()
                )
            },
        )
    }

    @Composable
    private fun appearanceGroup(
        applicationPreference: ApplicationPreference,
        navigator: Navigator,
    ): Preference {
        val timeFormatPreference = applicationPreference.timeFormat()
        val languagePreference = applicationPreference.language()
        val localeOptions = localeOptions()
        return Preference.PreferenceGroup(
            title = stringResource(R.string.appearance),
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.theme),
                    subtitle = "Dark", // TODO R.string.theme_dark, R.string.theme_light, R.string.theme_system
                    onClick = {
                        // TODO
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.language),
                    subtitle = localeOptions.filter {
                        it.value == languagePreference.get()
                    }.keys.firstOrNull(),
                    onClick = {
                        navigator.push(SettingsLanguageScreen)
                    }
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = timeFormatPreference,
                    title = stringResource(R.string.time_format_24h),
                    onValueChanged = { timeFormatPreference.toggle() }
                )
            )
        )
    }

    @Composable
    private fun aboutGroup(

    ): Preference {
        return Preference.PreferenceGroup(
            title = stringResource(R.string.about),
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.version),
                    subtitle = "1.0.0", // TODO
                ),
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.open_source_licenses),
                    onClick = {
                        // TODO
                    }
                ),
            )
        )
    }

}