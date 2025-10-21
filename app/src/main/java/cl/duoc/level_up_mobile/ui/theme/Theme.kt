package cl.duoc.level_up_mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tu paleta de colores gamer
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00FF00),        // --colorVerde
    onPrimary = Color(0xFF000000),      // --colorNegro
    primaryContainer = Color(0xFF00CC00), // --colorVerdeOscuro
    onPrimaryContainer = Color(0xFF000000),

    secondary = Color(0xFF0066FF),      // --colorAzul
    onSecondary = Color(0xFFFFFFFF),    // --colorBlanco

    background = Color(0xFF000000),     // --colorNegro
    onBackground = Color(0xFFFFFFFF),   // --colorBlanco

    surface = Color(0xFF1A1A1A),        // --colorGrisOscuro
    onSurface = Color(0xFFFFFFFF),      // --colorBlanco

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