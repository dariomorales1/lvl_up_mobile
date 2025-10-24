package cl.duoc.level_up_mobile.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch


data class DrawerItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String,
    val requiresAuth: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDrawer(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    currentRoute: String,
    isUserLoggedIn: Boolean = false,
    onItemClick: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    val drawerItems = listOf(
        DrawerItem("Inicio", Icons.Default.Home, "inicio"),
        DrawerItem("Catálogo", Icons.Default.Category, "catalogo"),
        DrawerItem("Favoritos", Icons.Default.Favorite, "favoritos", true),
        DrawerItem("Historial", Icons.Default.History, "historial", true),
        DrawerItem("Mi Perfil", Icons.Default.Person, "perfil", true),
        DrawerItem("Configuración", Icons.Default.Settings, "configuracion", true),

        DrawerItem("Blog y Tendencias", Icons.Default.Article, "blog"),
        DrawerItem("Contáctanos", Icons.Default.HeadsetMic, "contacto"),
        DrawerItem(
            if (isUserLoggedIn) "Cerrar Sesión" else "Iniciar Sesión",
            if (isUserLoggedIn) Icons.Default.ExitToApp else Icons.Default.Login,
            if (isUserLoggedIn) "logout" else "login"
        )


    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "🎮 LEVEL-UP GAMER",
                    modifier = Modifier.padding(16.dp)
                )
                drawerItems.forEach { item ->
                    if (!item.requiresAuth || isUserLoggedIn) {
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                onItemClick(item.route)
                            }
                        )
                    }
                }
            }
        },
        content = content
    )
}