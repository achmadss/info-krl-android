package dev.achmad.comuline.screens.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.comuline.base.ApplicationPreference
import dev.achmad.comuline.components.preference.Preference
import dev.achmad.comuline.components.preference.PreferenceScreen
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
            title = "Settings",
            shadowElevation = 4.dp,
            onBackPressed = {
                navigator.pop()
            },
            itemsProvider = {
                listOf(
                    appearanceGroup(appPreference),
                    aboutGroup()
                )
            },
        )
    }

    @Composable
    private fun appearanceGroup(
        applicationPreference: ApplicationPreference,
    ): Preference {
        val timeFormatPreference = applicationPreference.timeFormat()
        return Preference.PreferenceGroup(
            title = "Appearance",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Theme",
                    subtitle = "Dark", // TODO
                    onClick = {
                        // TODO
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Language",
                    subtitle = "English", // TODO
                    onClick = {
                        // TODO
                    }
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = timeFormatPreference,
                    title = "24-hour format",
                    onValueChanged = { timeFormatPreference.toggle() }
                )
            )
        )
    }

    @Composable
    private fun aboutGroup(

    ): Preference {
        return Preference.PreferenceGroup(
            title = "About",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Version",
                    subtitle = "1.0.0", // TODO
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Open source licenses",
                    onClick = {
                        // TODO
                    }
                ),
            )
        )
    }

}