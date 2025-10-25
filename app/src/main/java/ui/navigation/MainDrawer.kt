package cl.duoc.level_up_mobile.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.level_up_mobile.model.User
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
    currentUser: User?, // Cambiado: ahora recibe User en lugar de isUserLoggedIn
    onItemClick: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isUserLoggedIn = currentUser != null

    val drawerItems = remember(isUserLoggedIn) {
        if (isUserLoggedIn) {
            // Drawer para usuario LOGUEADO
            listOf(
                DrawerItem("Inicio", Icons.Default.Home, "inicio"),
                DrawerItem("Catálogo", Icons.Default.Category, "catalogo"),
                DrawerItem("Favoritos", Icons.Default.Favorite, "favoritos"),
                DrawerItem("Historial", Icons.Default.History, "historial"),
                DrawerItem("Mi Perfil", Icons.Default.Person, "perfil"),
                DrawerItem("Configuración", Icons.Default.Settings, "configuracion"),
                DrawerItem("Cerrar Sesión", Icons.Default.ExitToApp, "logout")
            )
        } else {
            // Drawer para usuario NO logueado
            listOf(
                DrawerItem("Inicio", Icons.Default.Home, "inicio"),
                DrawerItem("Catálogo", Icons.Default.Category, "catalogo"),
                DrawerItem("Iniciar Sesión", Icons.Default.Login, "login")
            )
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Header que cambia según el estado
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "🎮 LEVEL-UP GAMER",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isUserLoggedIn) {
                        // Header para usuario logueado
                        Column {
                            Text(
                                "¡Hola!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            currentUser?.email?.let { email ->
                                Text(
                                    email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        // Header para usuario no logueado
                        Text(
                            "Inicia sesión para más funciones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                // Items del menú
                drawerItems.forEach { item ->
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
        },
        content = content
    )
}