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
    primary = TungstenAmber,
    onPrimary = ObsidianBlack,
    primaryContainer = CarbonInteractive,
    onPrimaryContainer = TextWhite,
    secondary = CobaltBeam,
    onSecondary = TextWhite,
    secondaryContainer = CarbonElevated,
    onSecondaryContainer = TextWhite,
    tertiary = SignalEmerald,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TextWhite,
    surface = MatteCarbon,
    onSurface = TextWhite,
    surfaceVariant = CarbonElevated,
    onSurfaceVariant = TextMuted,
    outline = HairlineBorder,
    outlineVariant = PrecisionGrid
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
