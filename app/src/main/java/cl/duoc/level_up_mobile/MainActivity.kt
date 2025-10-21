package cl.duoc.level_up_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.material3.rememberDrawerState
import cl.duoc.level_up_mobile.repository.productos.ProductoRepository
import cl.duoc.level_up_mobile.ui.navigation.MainDrawer
import cl.duoc.level_up_mobile.ui.screens.HomeScreen
import cl.duoc.level_up_mobile.ui.theme.LevelUp_MobileTheme
import kotlinx.coroutines.launch
import cl.duoc.level_up_mobile.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val productoRepository = ProductoRepository(this)

        setContent {
            LevelUp_MobileTheme {
                val drawerState = rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                MainDrawer(
                    drawerState = drawerState,
                    currentRoute = "inicio",
                    isUserLoggedIn = false,
                    onItemClick = { route ->
                        println("Navegar a: $route")
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            context = this,
                            productoRepository = productoRepository,
                            drawerState = drawerState,
                            onMenuClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            },
                            onCartClick = {
                                println("Abrir carrito de compras")
                            }
                        )
                    }
                }
            }
        }
    }
}