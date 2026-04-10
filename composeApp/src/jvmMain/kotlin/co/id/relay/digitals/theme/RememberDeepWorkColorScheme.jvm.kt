package co.id.relay.digitals.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun rememberDeepWorkColorScheme(darkTheme: Boolean): ColorScheme =
    if (darkTheme) deepWorkDarkColorScheme() else deepWorkLightColorScheme()
