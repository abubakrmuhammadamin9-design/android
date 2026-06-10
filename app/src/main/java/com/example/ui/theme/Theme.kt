package com.example.ui.theme

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ImmersivePrimary,
    onPrimary = ImmersiveOnPrimary,
    primaryContainer = ImmersivePrimaryContainer,
    onPrimaryContainer = ImmersiveOnPrimaryContainer,
    secondary = ImmersiveSecondary,
    onSecondary = ImmersiveOnSecondary,
    background = ImmersiveBackground,
    onBackground = ImmersiveTextPrimary,
    surface = ImmersiveSurface,
    onSurface = ImmersiveTextPrimary,
    surfaceVariant = ImmersiveSurfaceVariant,
    onSurfaceVariant = ImmersiveTextSecondary,
    outline = ImmersiveOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = ImmersiveHighlight,
    onPrimary = ImmersiveOnPrimaryContainer,
    primaryContainer = ImmersivePrimaryDark,
    onPrimaryContainer = ImmersivePrimaryContainer,
    secondary = ImmersiveSecondary,
    onSecondary = ImmersiveOnSecondary,
    background = ImmersiveTextPrimary,
    onBackground = ImmersiveBackground,
    surface = Color(0xFF211F24),
    onSurface = ImmersiveBackground,
    surfaceVariant = Color(0xFF2F2D32),
    onSurfaceVariant = ImmersiveHighlight,
    outline = ImmersiveOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false by default to ensure the elegant custom branding is fully preserved
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
