package co.id.relay.digitals.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val DeepInk = Color(0xFF0E1114)
private val DeepSurface = Color(0xFF151A20)
private val DeepSurfaceVariant = Color(0xFF1E252D)
private val DeepOnBackground = Color(0xFFE6EAEE)
private val DeepOnSurface = Color(0xFFDDE3E9)
private val DeepOnSurfaceVariant = Color(0xFF9AA5B0)
private val DeepOutline = Color(0xFF3D4653)
private val DeepOutlineVariant = Color(0xFF2A323C)

private val CalmTeal = Color(0xFF6FA8AE)
private val CalmTealContainer = Color(0xFF2A4549)
private val OnCalmTealContainer = Color(0xFFB8D9DD)
private val OnCalmTeal = Color(0xFF062022)

private val MutedIndigo = Color(0xFF8B95B0)
private val MutedIndigoContainer = Color(0xFF2E3448)
private val OnMutedIndigoContainer = Color(0xFFC8CDE3)

private val DeepError = Color(0xFFFFB4AB)
private val DeepErrorContainer = Color(0xFF93000A)
private val OnDeepError = Color(0xFF690005)

private val PaperWarm = Color(0xFFF6F2EC)
private val PaperSurface = Color(0xFFFFFBF7)
private val PaperSurfaceVariant = Color(0xFFE8E3DC)
private val InkLight = Color(0xFF1A1F26)
private val InkSecondary = Color(0xFF4A5563)
private val InkTertiary = Color(0xFF6B7280)

private val LightTeal = Color(0xFF3D6569)
private val LightTealContainer = Color(0xFFB8D9DD)
private val OnLightTealContainer = Color(0xFF0A2528)

private val LightOutline = Color(0xFF6B7280)
private val LightOutlineVariant = Color(0xFFC4C9D0)

fun deepWorkDarkColorScheme() = darkColorScheme(
    primary = CalmTeal,
    onPrimary = OnCalmTeal,
    primaryContainer = CalmTealContainer,
    onPrimaryContainer = OnCalmTealContainer,
    secondary = MutedIndigo,
    onSecondary = Color(0xFF161924),
    secondaryContainer = MutedIndigoContainer,
    onSecondaryContainer = OnMutedIndigoContainer,
    tertiary = Color(0xFF7A9E86),
    onTertiary = Color(0xFF0D1F14),
    tertiaryContainer = Color(0xFF2A4032),
    onTertiaryContainer = Color(0xFFC5E5CF),
    error = DeepError,
    onError = OnDeepError,
    errorContainer = DeepErrorContainer,
    onErrorContainer = DeepError,
    background = DeepInk,
    onBackground = DeepOnBackground,
    surface = DeepSurface,
    onSurface = DeepOnSurface,
    surfaceVariant = DeepSurfaceVariant,
    onSurfaceVariant = DeepOnSurfaceVariant,
    outline = DeepOutline,
    outlineVariant = DeepOutlineVariant,
    scrim = Color(0xFF000000),
    inverseSurface = DeepOnSurface,
    inverseOnSurface = DeepSurface,
    inversePrimary = Color(0xFF3D6569),
)

fun deepWorkLightColorScheme() = lightColorScheme(
    primary = LightTeal,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = LightTealContainer,
    onPrimaryContainer = OnLightTealContainer,
    secondary = Color(0xFF4F5B7A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6DAEB),
    onSecondaryContainer = Color(0xFF1A2233),
    tertiary = Color(0xFF3D5F4A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC5E5CF),
    onTertiaryContainer = Color(0xFF0D2818),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = PaperWarm,
    onBackground = InkLight,
    surface = PaperSurface,
    onSurface = InkLight,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = Color(0xFF000000),
    inverseSurface = InkLight,
    inverseOnSurface = PaperWarm,
    inversePrimary = LightTealContainer,
)
