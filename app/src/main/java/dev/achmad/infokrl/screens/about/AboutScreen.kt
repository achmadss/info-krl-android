package dev.achmad.infokrl.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.infokrl.BuildConfig
import dev.achmad.infokrl.R
import dev.achmad.infokrl.components.AppBar
import dev.achmad.infokrl.components.LinkIcon
import dev.achmad.infokrl.components.preference.widget.TextPreferenceWidget
import dev.achmad.infokrl.components.LogoHeader

object AboutScreen: Screen {
    private fun readResolve(): Any = AboutScreen

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current

        AboutScreen(
            onNavigateUp = {
                navigator.pop()
            },
            onWebsiteClick = {
                uriHandler.openUri("https://achmad.dev")
            },
            onGithubClick = {
                uriHandler.openUri("https://github.com/achmadss/info-krl-android")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutScreen(
    onNavigateUp: () -> Unit = {},
    onClickVersion: () -> Unit = {},
    onClickChangelog: () -> Unit = {},
    onClickCredits: () -> Unit = {},
    onGithubClick: () -> Unit = {},
    onWebsiteClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp
            ) {
                AppBar(
                    title = stringResource(R.string.about),
                    navigateUp = onNavigateUp,
                )
            }
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { LogoHeader() }
            item { HorizontalDivider() }
            item {
                TextPreferenceWidget(
                    title = stringResource(R.string.version),
                    subtitle = BuildConfig.VERSION_NAME,
                    onPreferenceClick = onClickVersion
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(R.string.changelog),
                    onPreferenceClick = onClickChangelog
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(R.string.credits),
                    onPreferenceClick = onClickCredits
                )
            }
            item { HorizontalDivider() }
            item {
                TextPreferenceWidget(
                    title = "Website",
                    icon = Icons.Default.Public,
                    onPreferenceClick = onWebsiteClick
                )
            }
            item {
                TextPreferenceWidget(
                    title = "Github",
                    onPreferenceClick = onGithubClick
                )
            }
        }
    }
}