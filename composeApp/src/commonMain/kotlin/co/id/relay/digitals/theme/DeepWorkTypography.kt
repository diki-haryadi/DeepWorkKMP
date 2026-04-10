package co.id.relay.digitals.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import deepwork.composeapp.generated.resources.Res
import deepwork.composeapp.generated.resources.dm_sans_variable
import org.jetbrains.compose.resources.Font

@Composable
fun deepWorkTypography(): Typography {
    val font = FontFamily(
        Font(Res.font.dm_sans_variable, FontWeight.Normal),
        Font(Res.font.dm_sans_variable, FontWeight.Medium),
        Font(Res.font.dm_sans_variable, FontWeight.SemiBold),
    )
    val default = Typography()
    return Typography(
        displayLarge = default.displayLarge.withFont(font).tunedForFocus(57, 64),
        displayMedium = default.displayMedium.withFont(font).tunedForFocus(45, 52),
        displaySmall = default.displaySmall.withFont(font).tunedForFocus(36, 44),
        headlineLarge = default.headlineLarge.withFont(font).tunedForFocus(32, 40),
        headlineMedium = default.headlineMedium.withFont(font).tunedForFocus(28, 36),
        headlineSmall = default.headlineSmall.withFont(font).tunedForFocus(24, 32),
        titleLarge = default.titleLarge.withFont(font).tunedForFocus(22, 28),
        titleMedium = default.titleMedium.withFont(font, FontWeight.SemiBold).tunedForFocus(16, 24),
        titleSmall = default.titleSmall.withFont(font, FontWeight.Medium).tunedForFocus(14, 20),
        bodyLarge = default.bodyLarge.withFont(font).tunedForFocus(16, 24),
        bodyMedium = default.bodyMedium.withFont(font).tunedForFocus(14, 22),
        bodySmall = default.bodySmall.withFont(font).tunedForFocus(12, 18),
        labelLarge = default.labelLarge.withFont(font, FontWeight.Medium).tunedForFocus(14, 20),
        labelMedium = default.labelMedium.withFont(font, FontWeight.Medium).tunedForFocus(12, 16),
        labelSmall = default.labelSmall.withFont(font, FontWeight.Medium).tunedForFocus(11, 16),
    )
}

private fun TextStyle.withFont(
    fontFamily: FontFamily,
    weight: FontWeight? = null,
): TextStyle = copy(
    fontFamily = fontFamily,
    fontWeight = weight ?: this.fontWeight,
)

private fun TextStyle.tunedForFocus(
    sizeSp: Int,
    lineHeightSp: Int,
): TextStyle = copy(
    fontSize = sizeSp.sp,
    lineHeight = lineHeightSp.sp,
)
