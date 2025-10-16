package dev.achmad.comuline.screens.settings.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.comuline.R
import dev.achmad.comuline.base.ApplicationPreference
import dev.achmad.comuline.components.preference.Preference
import dev.achmad.comuline.components.preference.PreferenceScreen
import dev.achmad.comuline.util.collectAsState
import dev.achmad.core.di.util.injectLazy

object SettingsLanguageScreen: Screen {
    private fun readResolve(): Any = SettingsLanguageScreen

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val applicationPreference by injectLazy<ApplicationPreference>()

        PreferenceScreen(
            title = stringResource(R.string.language),
            itemsProvider = { languages(applicationPreference) },
            onBackPressed = { navigator.pop() }
        )
    }

    @Composable
    private fun languages(
        applicationPreference: ApplicationPreference,
    ): List<Preference> {
        val localeOptions = localeOptions()
        val languagePref = applicationPreference.language()
        val language by languagePref.collectAsState()

        return localeOptions.map { option ->
            Preference.PreferenceItem.CheckPreference(
                value = option.value,
                checked = language == option.value,
                title = option.key,
                onClick = { value ->
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(value)
                    )
                    languagePref.set(value)
                },
            )
        }
    }

}
