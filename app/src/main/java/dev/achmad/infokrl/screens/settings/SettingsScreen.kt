package dev.achmad.infokrl.screens.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dev.achmad.core.util.injectLazy
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
        val appUpdateManager by injectLazy<AppUpdateManager>()

        val updateResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                // Update flow was cancelled or failed
            }
        }

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
                        onConfirmClearData = {
                            screenModel.wipeAllData()
                        }
                    ),
                    aboutGroup(
                        navigator = navigator,
                        appUpdateManager = appUpdateManager,
                        appPreference = appPreference,
                        updateResultLauncher = updateResultLauncher
                    ),
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
        appUpdateManager: AppUpdateManager,
        appPreference: ApplicationPreference,
        updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
    ): Preference {
        val checkForUpdateOnLaunchPreference = appPreference.checkForUpdateOnLaunch()

        return Preference.PreferenceGroup(
            title = stringResource(R.string.about),
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.version),
                    subtitle = BuildConfig.VERSION_NAME,
                ),
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(R.string.check_for_updates),
                    onClick = {
                        checkForUpdates(
                            appUpdateManager = appUpdateManager,
                            updateResultLauncher = updateResultLauncher
                        )
                    }
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = checkForUpdateOnLaunchPreference,
                    title = stringResource(R.string.check_for_update_on_launch),
                    onValueChanged = { checkForUpdateOnLaunchPreference.toggle() }
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

    private fun checkForUpdates(
        appUpdateManager: AppUpdateManager,
        updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val updateAvailability = appUpdateInfo.updateAvailability()
            if (updateAvailability == UpdateAvailability.UPDATE_AVAILABLE) {
                val updateType = when {
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
                    else -> null
                }
                
                updateType?.let {
                    try {
                        val options = AppUpdateOptions.newBuilder(it).build()
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            updateResultLauncher,
                            options
                        )
                    } catch (e: Exception) {
                        // Handle error - update flow could not be started
                    }
                }
            }
        }.addOnFailureListener {
            // Handle failure - could not check for updates
        }
    }

    @Composable
    private fun dataGroup(
        onConfirmClearData: () -> Unit = {},
    ): Preference {
        return Preference.PreferenceGroup(
            title = stringResource(R.string.data),
            preferenceItems = listOf(
                Preference.PreferenceItem.AlertDialogPreference(
                    title = stringResource(R.string.clear_local_data),
                    subtitle = stringResource(R.string.clear_local_data_subtitle),
                    dialogTitle = stringResource(R.string.warning),
                    dialogText = stringResource(R.string.clear_local_data_description),
                    onConfirm = onConfirmClearData,
                    onCancel = {}
                )
            )
        )
    }
}