package dev.achmad.infokrl.screens.settings.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.infokrl.R
import dev.achmad.infokrl.components.preference.Preference
import dev.achmad.infokrl.components.preference.PreferenceScreen

object SettingsLanguageScreen: Screen {
    private fun readResolve(): Any = SettingsLanguageScreen

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        PreferenceScreen(
            title = stringResource(R.string.language),
            itemsProvider = { languages() },
            onBackPressed = { navigator.pop() }
        )
    }

    @Composable
    private fun languages(): List<Preference> {
        val locale = LocalConfiguration.current.locales[0]
        val localeOptions = localeOptions()

        return localeOptions.map { option ->
            Preference.PreferenceItem.CheckPreference(
                value = option.value,
                checked = option.value == locale.language,
                title = option.key,
                onClick = { value ->
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(value)
                    )
                },
            )
        }
    }

}
