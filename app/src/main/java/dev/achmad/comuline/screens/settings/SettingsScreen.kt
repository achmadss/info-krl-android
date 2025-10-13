package dev.achmad.comuline.screens.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.comuline.components.preference.Preference
import dev.achmad.comuline.components.preference.PreferenceScreen

object SettingsScreen : Screen {
    private fun readResolve(): Any = SettingsScreen

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        PreferenceScreen(
            title = "Settings",
            shadowElevation = 4.dp,
            onBackPressed = {
                navigator.pop()
            },
            itemsProvider = {
                listOf(
                    language()
                )
            },
        )
    }

    @Composable
    private fun language(

    ): Preference {
        return Preference.PreferenceItem.TextPreference(
            title = "Language",
            subtitle = "English",
            onClick = {
                // TODO
            }
        )
    }

}