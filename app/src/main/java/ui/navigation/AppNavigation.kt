package cl.duoc.level_up_mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cl.duoc.level_up_mobile.model.Producto
import cl.duoc.level_up_mobile.ui.screens.HomeScreen
import cl.duoc.level_up_mobile.ui.screens.ProductDetailScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

sealed class Screen {
    object Home : Screen()
    data class ProductDetail(val producto: Producto) : Screen()
}

@Composable
fun AppNavigation(
    context: android.content.Context,
    productoRepository: cl.duoc.level_up_mobile.repository.productos.ProductoRepository,
    drawerState: androidx.compose.material3.DrawerState,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val currentScreen = remember { mutableStateOf<Screen>(Screen.Home) }
    val selectedProduct = remember { mutableStateOf<Producto?>(null) }

    when (val screen = currentScreen.value) {
        is Screen.Home -> {
            HomeScreen(
                productoRepository = productoRepository,
                onMenuClick = onMenuClick,
                onProductClick = { producto ->
                    selectedProduct.value = producto
                    currentScreen.value = Screen.ProductDetail(producto)
                },
                onCartClick = onCartClick,
                context = context
            )
        }
        is Screen.ProductDetail -> {
            ProductDetailScreen(
                producto = screen.producto,
                context = context,
                onBackClick = {
                    currentScreen.value = Screen.Home
                },
                onAddToCart = { producto ->
                    // Aquí agregaremos la lógica del carrito después
                    println("Producto añadido al carrito: ${producto.nombre}")
                }
            )
        }
    }
}