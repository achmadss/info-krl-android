package dev.achmad.infokrl.screens.settings.credits

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.infokrl.R
import dev.achmad.infokrl.components.preference.Preference
import dev.achmad.infokrl.components.preference.PreferenceScreen
import dev.achmad.infokrl.components.preference.widget.TextPreferenceWidget

object CreditsScreen: Screen {
    private fun readResolve(): Any = CreditsScreen

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        PreferenceScreen(
            title = stringResource(R.string.credits),
            itemsProvider = {
                listOf(
                    iconGroup(),
                    googleGroup(),
                    jetbrainsGroup(),
                    squareGroup(),
                    koinGroup(),
                    voyagerGroup(),
                    coilGroup(),
                    materialMotionGroup(),
                    chuckerGroup(),
                )
            },
            onBackPressed = {
                navigator.pop()
            }
        )
    }

    @Composable
    private fun iconGroup(): Preference {
        return Preference.PreferenceGroup(
            title = "Flaticon",
            preferenceItems = listOf(
                Preference.PreferenceItem.CustomPreference {
                    TextPreferenceWidget(
                        title = stringResource(R.string.credits_train_icon),
                        subtitle = stringResource(R.string.credits_train_icon_attribution),
                        widget = {
                            Icon(
                                modifier = Modifier.size(48.dp),
                                painter = painterResource(R.drawable.train),
                                contentDescription = null,
                            )
                        }
                    )
                }
            )
        )
    }

    @Composable
    private fun googleGroup(): Preference {
        return Preference.PreferenceGroup(
            title = "Google / Android",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Core KTX"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Lifecycle Runtime KTX"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Activity Compose"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Compose BOM"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Compose UI"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Compose Material3"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Material Icons Extended"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX AppCompat"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Room"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX WorkManager"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Core Splashscreen"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Google Material Design Components"
                ),
            )
        )
    }

    @Composable
    private fun jetbrainsGroup(): Preference {
        return Preference.PreferenceGroup(
            title = "JetBrains",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Kotlin Standard Library"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Kotlinx Serialization JSON"
                ),
            )
        )
    }

    @Composable
    private fun squareGroup(): Preference {
        return Preference.PreferenceGroup(
            title = "Square / Block",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "OkHttp"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "OkHttp Logging Interceptor"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "OkHttp Brotli"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "OkHttp DNS over HTTPS"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Okio"
                ),
            )
        )
    }

    @Composable
    private fun koinGroup(): Preference {
        return Preference.PreferenceGroup(
            title = "Koin",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Koin for Android"
                ),
            )
        )
    }

    @Composable
    private fun voyagerGroup(): Preference {
        return Preference.PreferenceGroup(
            title = "Voyager",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Voyager Navigator"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Voyager Tab Navigator"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Voyager Transitions"
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Voyager ScreenModel"
                ),
            )
        )
    }

    @Composable
    private fun coilGroup(): Preference {
        return Preference.PreferenceGroup(
            title = "Coil",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Coil Compose"
                ),
            )
        )
    }

    @Composable
    private fun materialMotionGroup(): Preference {
        return Preference.PreferenceGroup(
            title = "Material Motion",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Material Motion Compose Core"
                ),
            )
        )
    }

    @Composable
    private fun chuckerGroup(): Preference {
        return Preference.PreferenceGroup(
            title = "Chucker",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Chucker (Network Inspector)"
                ),
            )
        )
    }

}