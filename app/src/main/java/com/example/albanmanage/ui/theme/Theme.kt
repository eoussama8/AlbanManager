package com.example.albanmanage.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Single light color scheme only
private val LightColorScheme = lightColorScheme(
    primary = AlbaneBlue,
    onPrimary = AlbaneWhite,
    secondary = AlbaneGrey,
    onSecondary = AlbaneWhite,
    tertiary = AlbaneGreen,
    onTertiary = AlbaneWhite,
    background = AlbaneWhite,
    onBackground = Color.Black,
    surface = AlbaneWhite,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242),
    error = AlbaneRed,
    onError = AlbaneWhite
)

@Composable
fun AlbanManageTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
