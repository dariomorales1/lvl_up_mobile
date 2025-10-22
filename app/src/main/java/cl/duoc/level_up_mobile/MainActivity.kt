package cl.duoc.level_up_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cl.duoc.level_up_mobile.repository.carrito.CarritoRepository
import cl.duoc.level_up_mobile.repository.productos.ProductoRepository
import cl.duoc.level_up_mobile.ui.navigation.AppNavigation
import cl.duoc.level_up_mobile.ui.navigation.MainDrawer
import cl.duoc.level_up_mobile.ui.navigation.Screen
import cl.duoc.level_up_mobile.ui.theme.LevelUp_MobileTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val productoRepository = ProductoRepository(this)
        val carritoRepository = CarritoRepository(this)

        setContent {
            LevelUp_MobileTheme {
                val drawerState = rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val currentScreen = remember { mutableStateOf<Screen>(Screen.Home) }

                MainDrawer(
                    drawerState = drawerState,
                    currentRoute = when (currentScreen.value) {
                        is Screen.Home -> "inicio"
                        is Screen.Catalog -> "catalogo"
                        is Screen.Cart -> "carrito"
                        else -> "inicio"
                    },
                    isUserLoggedIn = false,
                    onItemClick = { route ->
                        scope.launch { drawerState.close() }
                        when (route) {
                            "inicio" -> currentScreen.value = Screen.Home
                            "catalogo" -> currentScreen.value = Screen.Catalog
                            "carrito" -> currentScreen.value = Screen.Cart
                            "login" -> println("Navegar a login")
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            context = this,
                            productoRepository = productoRepository,
                            carritoRepository = carritoRepository,
                            drawerState = drawerState,
                            currentScreen = currentScreen.value,
                            onScreenChange = { newScreen ->
                                currentScreen.value = newScreen
                            },
                            onMenuClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            },
                            onCartClick = {
                                currentScreen.value = Screen.Cart
                            }
                        )
                    }
                }
            }
        }
    }
}