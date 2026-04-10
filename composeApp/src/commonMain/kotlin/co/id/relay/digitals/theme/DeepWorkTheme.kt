package co.id.relay.digitals.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun DeepWorkTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = rememberDeepWorkColorScheme(darkTheme)
    val typography = deepWorkTypography()
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = deepWorkShapes(),
        content = content,
    )
}
