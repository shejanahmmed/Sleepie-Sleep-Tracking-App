package com.shejan.sleepie.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TextPrimaryDark,
    onPrimary = ObsidianBlack,
    secondary = TextSecondaryDark,
    onSecondary = TextPrimaryDark,
    background = ObsidianBlack,
    onBackground = TextPrimaryDark,
    surface = SurfaceCardDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkCharcoal,
    outline = BorderDark,
    error = TechRedDark,
    onError = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = TextPrimaryLight,
    onPrimary = PureWhite,
    secondary = TextSecondaryLight,
    onSecondary = TextPrimaryLight,
    background = PureWhite,
    onBackground = TextPrimaryLight,
    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = OffWhite,
    outline = BorderLight,
    error = TechRedLight,
    onError = PureWhite
)

@Composable
fun SleepieTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}