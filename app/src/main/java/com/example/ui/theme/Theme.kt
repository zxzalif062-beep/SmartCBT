package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    secondary = PurpleAccentDark,
    tertiary = CyanAccent,
    background = SlateDarkBG,
    surface = CardDarkBG,
    onPrimary = WhiteBase,
    onSecondary = WhiteBase,
    onTertiary = SlateDarkBG,
    onBackground = WhiteBase,
    onSurface = WhiteBase,
    surfaceVariant = CardDarkBG,
    onSurfaceVariant = AccentDull
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = PurpleAccent,
    tertiary = BlueSecondary,
    background = SlateBackground,
    surface = WhiteBase,
    onPrimary = WhiteBase,
    onSecondary = WhiteBase,
    onTertiary = WhiteBase,
    onBackground = SlateText,
    onSurface = SlateText,
    surfaceVariant = SlateBorderLight,
    onSurfaceVariant = SlateTextMuted
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
