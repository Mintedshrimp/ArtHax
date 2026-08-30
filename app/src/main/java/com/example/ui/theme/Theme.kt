package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ArtHaxColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = TextWhite,
    primaryContainer = CardBackgroundElevated,
    onPrimaryContainer = TextWhite,
    secondary = NeonPink,
    onSecondary = TextWhite,
    secondaryContainer = CardBackground,
    onSecondaryContainer = TextWhite,
    tertiary = NeonGreen,
    onTertiary = TextWhite,
    background = CyberBackground,
    onBackground = TextWhite,
    surface = CardBackground,
    onSurface = TextWhite,
    surfaceVariant = CardBackgroundElevated,
    onSurfaceVariant = TextMuted,
    outline = BorderGlass,
    outlineVariant = GridLine
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = ArtHaxColorScheme,
        typography = Typography,
        content = content
    )
}
