package com.example.albanmanage.ui.theme

import android.os.Build
import android.content.Context
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.albanmanage.data.ThemeMode
import com.example.albanmanage.data.ThemePreferences

// Dark theme colors using Albane brand colors
private val DarkColorScheme = darkColorScheme(
    primary = AlbaneBlue,
    secondary = AlbaneRed,
    tertiary = AlbaneLightBlue,
    background = AlbaneWhite,
    surface = AlbaneGrey,
    onPrimary = AlbaneWhite,
    onSecondary = AlbaneWhite,
    onBackground = AlbaneGrey,
    onSurface = AlbaneWhite
)

// Light theme colors using Albane brand colors
private val LightColorScheme = lightColorScheme(
    primary = AlbaneBlue,
    secondary = AlbaneRed,
    tertiary = AlbaneLightBlue,
    background = AlbaneWhite,
    surface = AlbaneGrey,
    onPrimary = AlbaneWhite,
    onSecondary = AlbaneWhite,
    onBackground = AlbaneGrey,
    onSurface = AlbaneWhite
)

// Helper function to override dynamic colors with Albane branding
@RequiresApi(Build.VERSION_CODES.S)
private fun getAlbaneDynamicColors(isDark: Boolean, context: Context): ColorScheme {
    val dynamicScheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    return dynamicScheme.copy(
        primary = AlbaneBlue,
        secondary = AlbaneRed,
        tertiary = AlbaneLightBlue,
        background = AlbaneWhite,
        surface = AlbaneGrey,
        onPrimary = if (isDark) AlbaneWhite else AlbaneGrey,
        onSecondary = AlbaneWhite,
        onBackground = AlbaneGrey,
        onSurface = AlbaneWhite
    )
}

@Composable
fun AlbanManageTheme(
    themePreferences: ThemePreferences,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val themeMode = themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM).value

    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            getAlbaneDynamicColors(isDarkTheme, LocalContext.current)
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
