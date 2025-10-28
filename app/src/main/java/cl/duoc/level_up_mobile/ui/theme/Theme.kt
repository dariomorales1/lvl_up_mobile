package cl.duoc.level_up_mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00FF00),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF00CC00),
    onPrimaryContainer = Color(0xFF000000),

    secondary = Color(0xFF0066FF),
    onSecondary = Color(0xFFFFFFFF),

    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),

    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFFFFFFF),

    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFCCCCCC)
)

@Composable
fun LevelUp_MobileTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}