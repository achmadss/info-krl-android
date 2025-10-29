package dev.achmad.infokrl.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.util.Consumer
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.ScreenTransition
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dev.achmad.domain.preference.ApplicationPreference
import dev.achmad.infokrl.screens.home.HomeScreen
import dev.achmad.infokrl.screens.onboarding.OnboardingScreen
import dev.achmad.infokrl.theme.AppTheme
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.android.ext.android.inject
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance

class MainActivity : AppCompatActivity() {

    private val applicationPreference: ApplicationPreference by inject()
    private val appUpdateManager: AppUpdateManager by inject()

    private var isReady = false
    private var initialScreen: Screen = HomeScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (isReady) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )

        handlePreDraw()

        enableEdgeToEdge()
        setContent {
            AppTheme {
                val slideDistance = rememberSlideDistance()
                Navigator(
                    screen = initialScreen,
                    disposeBehavior = NavigatorDisposeBehavior(
                        disposeNestedNavigators = false,
                        disposeSteps = true,
                    )
                ) { navigator ->
                    ScreenTransition(
                        modifier = Modifier.fillMaxSize(),
                        navigator = navigator,
                        transition = {
                            materialSharedAxisX(
                                forward = navigator.lastEvent != StackEvent.Pop,
                                slideDistance = slideDistance,
                            )
                        },
                    )
                    HandleNewIntent(this@MainActivity, navigator)
                    CheckForUpdatesOnLaunch(
                        appUpdateManager = appUpdateManager,
                        applicationPreference = applicationPreference
                    )
                }
            }
        }
    }

    private fun handlePreDraw() {
        // Handle pre draw here (e.g. Splash Screen, fetch data, etc)

        // Check if user has completed onboarding
        val hasCompletedOnboarding = applicationPreference.hasCompletedOnboarding().get()
        initialScreen = if (hasCompletedOnboarding) {
            HomeScreen
        } else {
            OnboardingScreen
        }

        isReady = true
    }

    @Composable
    private fun HandleNewIntent(context: Context, navigator: Navigator) {
        LaunchedEffect(Unit) {
            callbackFlow {
                val componentActivity = context as ComponentActivity
                val consumer = Consumer<Intent> { trySend(it) }
                componentActivity.addOnNewIntentListener(consumer)
                awaitClose { componentActivity.removeOnNewIntentListener(consumer) }
            }.collectLatest { handleIntentAction(it, navigator) }
        }
    }

    private fun handleIntentAction(intent: Intent, navigator: Navigator) {
        // Handle intent here
    }

    @Composable
    private fun CheckForUpdatesOnLaunch(
        appUpdateManager: AppUpdateManager,
        applicationPreference: ApplicationPreference,
    ) {
        val updateResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                // Update flow was cancelled or failed
            }
        }

        LaunchedEffect(Unit) {
            val shouldCheckForUpdates = applicationPreference.checkForUpdateOnLaunch().get()
            if (shouldCheckForUpdates) {
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
        }
    }
}
