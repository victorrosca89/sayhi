package com.sayhi.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = TextWhite,
    primaryContainer = PurpleLight.copy(alpha = 0.2f),
    onPrimaryContainer = PurpleDark,
    secondary = PurpleLight,
    onSecondary = TextWhite,
    secondaryContainer = PurpleLight.copy(alpha = 0.15f),
    onSecondaryContainer = PurpleDark,
    tertiary = GreenOnline,
    onTertiary = TextWhite,
    background = LightBg,
    onBackground = TextPrimary,
    surface = LightSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF0F0F5),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFDFE6E9),
    outlineVariant = Color(0xFFEEEFF2),
    error = RedDanger,
    onError = TextWhite,
    errorContainer = RedBadge.copy(alpha = 0.1f),
    onErrorContainer = RedDanger,
    inverseSurface = DarkBgSecondary,
    inverseOnSurface = TextWhite,
    inversePrimary = PurpleLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleVivid,
    onPrimary = TextWhite,
    primaryContainer = PurpleDeep.copy(alpha = 0.3f),
    onPrimaryContainer = PurpleLight,
    secondary = PurpleLight,
    onSecondary = TextWhite,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = PurpleLight,
    tertiary = GreenOnline,
    onTertiary = TextWhite,
    background = DarkBgPrimary,
    onBackground = TextWhite,
    surface = DarkBgSecondary,
    onSurface = TextWhite,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = Color(0xFFB8B0D0),
    outline = DarkSurfaceBorder,
    outlineVariant = Color(0xFF2A1F45),
    error = RedDanger,
    onError = TextWhite,
    errorContainer = RedDanger.copy(alpha = 0.2f),
    onErrorContainer = RedBadge,
    inverseSurface = LightSurface,
    inverseOnSurface = TextPrimary,
    inversePrimary = PurplePrimary,
)

@Composable
fun SayHiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
