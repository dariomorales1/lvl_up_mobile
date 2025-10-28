package cl.duoc.level_up_mobile.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.level_up_mobile.model.User
import kotlinx.coroutines.launch

data class DrawerItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String,
    val requiresAuth: Boolean = false,
    val showComingSoon: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDrawer(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    currentRoute: String,
    currentUser: User?,
    onItemClick: (String) -> Unit,
    onShowComingSoonMessage: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isUserLoggedIn = currentUser != null

    val drawerItems = remember(isUserLoggedIn) {
        if (isUserLoggedIn) {
            listOf(
                DrawerItem("Inicio", Icons.Default.Home, "inicio"),
                DrawerItem("Catálogo", Icons.Default.Category, "catalogo"),
                DrawerItem("Favoritos", Icons.Default.Favorite, "favoritos", showComingSoon = true),
                DrawerItem("Historial", Icons.Default.History, "historial", showComingSoon = true),
                DrawerItem("Mi Perfil", Icons.Default.Person, "perfil"),
                DrawerItem("Configuración", Icons.Default.Settings, "configuracion", showComingSoon = true),
                DrawerItem("Cerrar Sesión", Icons.Default.ExitToApp, "logout")
            )
        } else {
            listOf(
                DrawerItem("Inicio", Icons.Default.Home, "inicio"),
                DrawerItem("Catálogo", Icons.Default.Category, "catalogo"),
                DrawerItem("Blog", Icons.Default.Interests, "blog"),
                DrawerItem("Contacto", Icons.Default.Contacts, "contacto"),
                DrawerItem("Iniciar Sesión", Icons.Default.Login, "login"),
                DrawerItem("Registrarse", Icons.Default.PersonAdd, "signup")
            )
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "LEVEL-UP GAMER",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isUserLoggedIn) {
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
                        Column {
                            Text(
                                "Bienvenido a LEVEL-UP",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Inicia sesión o regístrate para más funciones",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                // Items del menú
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = null
                            )
                        },
                        label = {
                            Row {
                                Text(item.title)
                                if (item.showComingSoon) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "*",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }

                            if (item.showComingSoon) {
                                onShowComingSoonMessage(item.title)
                            } else {
                                onItemClick(item.route)
                            }
                        }
                    )
                }
                //Version y copyright
                Spacer(modifier = Modifier.weight(1f))
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Versión 1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant

                    )
                    Text(
                        "© 2025 Level-Up Gamer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        content = content
    )
}