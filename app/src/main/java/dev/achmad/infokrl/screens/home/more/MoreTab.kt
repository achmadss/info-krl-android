package dev.achmad.infokrl.screens.home.more

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.preference.ApplicationPreference
import dev.achmad.infokrl.R
import dev.achmad.infokrl.components.AppBar
import dev.achmad.infokrl.components.LogoHeader
import dev.achmad.infokrl.components.preference.widget.ListPreferenceWidget
import dev.achmad.infokrl.components.preference.widget.TextPreferenceWidget
import dev.achmad.infokrl.screens.fare.FareCalculatorScreen
import dev.achmad.infokrl.screens.settings.SettingsScreen
import dev.achmad.infokrl.screens.settings.theme.themeOptions
import dev.achmad.infokrl.util.collectAsState

object MoreTab : Tab {
    private fun readResolve(): Any = MoreTab

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            return TabOptions(
                index = 2u,
                title = stringResource(R.string.more),
                icon = rememberVectorPainter(
                    when {
                        isSelected -> Icons.Default.MoreHoriz
                        else -> Icons.Outlined.MoreHoriz
                    }
                )
            )
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // Provide navigator to inner composable
        val navigator = LocalNavigator.currentOrThrow
        MoreTab(
            onClickFareCalc = {
                navigator.push(FareCalculatorScreen)
            },
            onClickSettings = {
                navigator.push(SettingsScreen)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreTab(
    onClickReport: () -> Unit = {},
    onClickFareCalc: () -> Unit = {},
    onClickSettings: () -> Unit = {},
) {
    val applicationPreference by injectLazy<ApplicationPreference>()
    val themePreference = applicationPreference.appTheme()
    val theme by themePreference.collectAsState()
    val themeOptions = themeOptions()

    Scaffold { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item { LogoHeader() }
            item { HorizontalDivider() }
            item {
                ListPreferenceWidget(
                    value = theme,
                    enabled = true,
                    title = stringResource(R.string.theme),
                    subtitle = themeOptions[theme],
                    icon = Icons.Outlined.Palette,
                    entries = themeOptions,
                    onValueChange = { newValue ->
                        themePreference.set(newValue)
                    }
                )
            }
            item { HorizontalDivider() }
            item {
                TextPreferenceWidget(
                    title = stringResource(R.string.fare_calculator),
                    icon = Icons.Outlined.Calculate,
                    onPreferenceClick = onClickFareCalc
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(R.string.report_problem),
                    icon = Icons.Outlined.Report,
                    onPreferenceClick = onClickReport
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(R.string.settings),
                    icon = Icons.Outlined.Settings,
                    onPreferenceClick = onClickSettings
                )
            }
        }
    }
}