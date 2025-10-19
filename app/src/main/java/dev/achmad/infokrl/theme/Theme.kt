package dev.achmad.infokrl.theme

import android.app.Activity
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.achmad.core.di.util.inject
import dev.achmad.infokrl.base.ApplicationPreference
import dev.achmad.infokrl.util.collectAsState

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val applicationPreference = inject<ApplicationPreference>()
    val theme by applicationPreference.appTheme().collectAsState()
    val systemBarColor = when (theme) {
        ApplicationPreference.Themes.DARK -> Color.Transparent
        ApplicationPreference.Themes.LIGHT -> Color.White
        else -> if (darkTheme) Color.Transparent else Color.White
    }

    val colorScheme = when (theme) {
        ApplicationPreference.Themes.DARK -> darkColorScheme()
        ApplicationPreference.Themes.LIGHT -> lightColorScheme()
        else -> if (darkTheme) darkColorScheme() else lightColorScheme()
    }
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
        StatusBarColor(systemBarColor)
        NavigationBarColor(systemBarColor)
    }
}

@Suppress("DEPRECATION")
@Composable
fun NavigationBarColor(color: Color) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.navigationBarColor = color.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun StatusBarColor(color: Color) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = color.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }
}