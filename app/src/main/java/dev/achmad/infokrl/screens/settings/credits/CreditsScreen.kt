package dev.achmad.infokrl.screens.settings.credits

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch

object CreditsScreen: Screen {
    private fun readResolve(): Any = CreditsScreen

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        var selectedLicense by remember { mutableStateOf<LicenseInfo?>(null) }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            PreferenceScreen(
                title = stringResource(R.string.credits),
                itemsProvider = {
                    listOf(
                        iconGroup(),
                        googleGroup { selectedLicense = it },
                        jetbrainsGroup { selectedLicense = it },
                        squareGroup { selectedLicense = it },
                        koinGroup { selectedLicense = it },
                        voyagerGroup { selectedLicense = it },
                        coilGroup { selectedLicense = it },
                        materialMotionGroup { selectedLicense = it },
                        chuckerGroup { selectedLicense = it },
                    )
                },
                onBackPressed = {
                    navigator.pop()
                }
            )

            selectedLicense?.let { license ->
                ModalBottomSheet(
                    onDismissRequest = {
                        selectedLicense = null
                    },
                    sheetState = sheetState,
                ) {
                    LicenseBottomSheet(
                        licenseInfo = license,
                        onDismiss = {
                            scope.launch {
                                sheetState.hide()
                                selectedLicense = null
                            }
                        }
                    )
                }
            }
        }
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
    private fun googleGroup(onLicenseClick: (LicenseInfo) -> Unit): Preference {
        val apacheLicense = stringResource(R.string.license_apache_2_0)
        return Preference.PreferenceGroup(
            title = "Google / Android",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Core KTX",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "AndroidX Core KTX",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Lifecycle Runtime KTX",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "AndroidX Lifecycle Runtime KTX",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Activity Compose",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "AndroidX Activity Compose",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Compose BOM",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "AndroidX Compose BOM",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Compose UI",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "AndroidX Compose UI",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Compose Material3",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "AndroidX Compose Material3",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Material Icons Extended",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "AndroidX Material Icons Extended",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX AppCompat",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "AndroidX AppCompat",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Room",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "AndroidX Room",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX WorkManager",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "AndroidX WorkManager",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "AndroidX Core Splashscreen",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "AndroidX Core Splashscreen",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Google Material Design Components",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "Google Material Design Components",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
            )
        )
    }

    @Composable
    private fun jetbrainsGroup(onLicenseClick: (LicenseInfo) -> Unit): Preference {
        val apacheLicense = stringResource(R.string.license_apache_2_0)
        return Preference.PreferenceGroup(
            title = "JetBrains",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Kotlin Standard Library",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "Kotlin Standard Library",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Kotlinx Serialization JSON",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "Kotlinx Serialization JSON",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
            )
        )
    }

    @Composable
    private fun squareGroup(onLicenseClick: (LicenseInfo) -> Unit): Preference {
        val apacheLicense = stringResource(R.string.license_apache_2_0)
        return Preference.PreferenceGroup(
            title = "Square / Block",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "OkHttp",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "OkHttp",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "OkHttp Logging Interceptor",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "OkHttp Logging Interceptor",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "OkHttp Brotli",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "OkHttp Brotli",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "OkHttp DNS over HTTPS",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "OkHttp DNS over HTTPS",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Okio",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "Okio",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
            )
        )
    }

    @Composable
    private fun koinGroup(onLicenseClick: (LicenseInfo) -> Unit): Preference {
        val apacheLicense = stringResource(R.string.license_apache_2_0)
        return Preference.PreferenceGroup(
            title = "Koin",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Koin for Android",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "Koin for Android",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
            )
        )
    }

    @Composable
    private fun voyagerGroup(onLicenseClick: (LicenseInfo) -> Unit): Preference {
        val apacheLicense = stringResource(R.string.license_apache_2_0)
        return Preference.PreferenceGroup(
            title = "Voyager",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Voyager Navigator",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "Voyager Navigator",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Voyager Tab Navigator",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "Voyager Tab Navigator",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Voyager Transitions",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "Voyager Transitions",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
                Preference.PreferenceItem.TextPreference(
                    title = "Voyager ScreenModel",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "Voyager ScreenModel",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
            )
        )
    }

    @Composable
    private fun coilGroup(onLicenseClick: (LicenseInfo) -> Unit): Preference {
        val apacheLicense = stringResource(R.string.license_apache_2_0)
        return Preference.PreferenceGroup(
            title = "Coil",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Coil Compose",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "Coil Compose",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
            )
        )
    }

    @Composable
    private fun materialMotionGroup(onLicenseClick: (LicenseInfo) -> Unit): Preference {
        val apacheLicense = stringResource(R.string.license_apache_2_0)
        return Preference.PreferenceGroup(
            title = "Material Motion",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Material Motion Compose Core",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "Material Motion Compose Core",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
            )
        )
    }

    @Composable
    private fun chuckerGroup(onLicenseClick: (LicenseInfo) -> Unit): Preference {
        val apacheLicense = stringResource(R.string.license_apache_2_0)
        return Preference.PreferenceGroup(
            title = "Chucker",
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = "Chucker (Network Inspector)",
                    subtitle = apacheLicense,
                    onClick = {
                        onLicenseClick(LicenseInfo(
                            libraryName = "Chucker (Network Inspector)",
                            licenseName = apacheLicense,
                            licenseResourceId = R.raw.apache_2_0
                        ))
                    }
                ),
            )
        )
    }

}