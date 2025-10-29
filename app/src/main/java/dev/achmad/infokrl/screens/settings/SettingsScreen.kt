package dev.achmad.infokrl.screens.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.core.di.util.injectLazy
import dev.achmad.core.preference.toggle
import dev.achmad.domain.preference.ApplicationPreference
import dev.achmad.infokrl.BuildConfig
import dev.achmad.infokrl.R
import dev.achmad.infokrl.components.preference.Preference
import dev.achmad.infokrl.components.preference.PreferenceScreen
import dev.achmad.infokrl.screens.settings.credits.CreditsScreen
import dev.achmad.infokrl.screens.settings.language.SettingsLanguageScreen
import dev.achmad.infokrl.screens.settings.language.localeOptions
import dev.achmad.infokrl.screens.settings.theme.themeOptions

object SettingsScreen : Screen {

    private fun readResolve(): Any = SettingsScreen

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { SettingsScreenModel() }
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
                    dataGroup(
                        onClickClearData = {
                            screenModel.wipeAllData()
                        }
                    ),
                    aboutGroup(navigator),
                )
            },
        )
    }

    @Composable
    private fun appearanceGroup(
        applicationPreference: ApplicationPreference,
        navigator: Navigator,
    ): Preference {
        val themePreference = applicationPreference.appTheme()
        val timeFormatPreference = applicationPreference.is24HourFormat()
        val themeOptions = themeOptions()
        val locale = LocalConfiguration.current.locales[0]
        val localeOptions = localeOptions()
        return Preference.PreferenceGroup(
            title = stringResource(R.string.appearance),
            preferenceItems = listOf(
                Preference.PreferenceItem.ListPreference(
                    title = stringResource(R.string.theme),
                    preference = themePreference,
                    entries = themeOptions,
                ),
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.language),
                    subtitle = localeOptions.filter {
                        it.value == locale.language
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
        navigator: Navigator,
    ): Preference {
        return Preference.PreferenceGroup(
            title = stringResource(R.string.about),
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.version),
                    subtitle = BuildConfig.VERSION_NAME,
                ),
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.credits),
                    onClick = {
                        navigator.push(CreditsScreen)
                    }
                ),
            )
        )
    }

    @Composable
    private fun dataGroup(
        onClickClearData: () -> Unit = {},
    ): Preference {
        return Preference.PreferenceGroup(
            title = stringResource(R.string.data),
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.clear_local_data),
                    subtitle = stringResource(R.string.clear_local_data_warning),
                    onClick = onClickClearData
                )
            )
        )
    }
}