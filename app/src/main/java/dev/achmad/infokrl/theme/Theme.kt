package dev.achmad.infokrl.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.achmad.core.di.util.inject
import dev.achmad.domain.preference.ApplicationPreference
import dev.achmad.domain.theme.Themes
import dev.achmad.infokrl.util.collectAsState

val LocalColorScheme = compositionLocalOf { lightColorScheme() }
val darkTheme = darkColorScheme()
val lightTheme = lightColorScheme()

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val applicationPreference = inject<ApplicationPreference>()
    val theme by applicationPreference.appTheme().collectAsState()
    val systemBarColor = when (theme) {
        Themes.DARK -> Color.Transparent
        Themes.LIGHT -> Color.White
        else -> if (isDarkTheme) Color.Transparent else Color.White
    }

    val colorScheme = when (theme) {
        Themes.DARK -> darkTheme
        Themes.LIGHT -> lightTheme
        else -> if (isDarkTheme) darkTheme else lightTheme
    }
    CompositionLocalProvider(
        LocalColorScheme provides colorScheme
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background),
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography,
                content = content,
            )
            StatusBarColor(systemBarColor, colorScheme)
            NavigationBarColor(systemBarColor, colorScheme)
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun NavigationBarColor(color: Color, colorScheme: ColorScheme) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.navigationBarColor = color.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val windowInsetsController = WindowInsetsControllerCompat(window, view)
            windowInsetsController.isAppearanceLightNavigationBars = colorScheme == lightTheme
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun StatusBarColor(color: Color, colorScheme: ColorScheme) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = color.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val windowInsetsController = WindowInsetsControllerCompat(window, view)
            windowInsetsController.isAppearanceLightStatusBars = colorScheme == lightTheme
        }
    }
}