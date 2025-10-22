package cl.duoc.level_up_mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cl.duoc.level_up_mobile.model.Producto
import cl.duoc.level_up_mobile.ui.screens.CartScreen
import cl.duoc.level_up_mobile.ui.screens.CatalogScreen
import cl.duoc.level_up_mobile.ui.screens.CategoryProductsScreen
import cl.duoc.level_up_mobile.ui.screens.HomeScreen
import cl.duoc.level_up_mobile.ui.screens.ProductDetailScreen
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

sealed class Screen {
    object Home : Screen()
    data class ProductDetail(val producto: Producto) : Screen()
    object Catalog : Screen()
    data class CategoryProducts(val categoria: String) : Screen()
    object Cart : Screen()
}

@Composable
fun AppNavigation(
    context: android.content.Context,
    productoRepository: cl.duoc.level_up_mobile.repository.productos.ProductoRepository,
    carritoRepository: cl.duoc.level_up_mobile.repository.carrito.CarritoRepository,
    drawerState: androidx.compose.material3.DrawerState,
    currentScreen: Screen = Screen.Home,
    onScreenChange: (Screen) -> Unit = {},
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val selectedProduct = remember { mutableStateOf<Producto?>(null) }
    val selectedCategory = remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    when (currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                productoRepository = productoRepository,
                onMenuClick = onMenuClick,
                onProductClick = { producto ->
                    selectedProduct.value = producto
                    onScreenChange(Screen.ProductDetail(producto))
                },
                onCartClick = onCartClick,
                context = context,
                onAddToCart = { producto -> // ← NUEVO CALLBACK
                    coroutineScope.launch {
                        carritoRepository.agregarProducto(producto, 1)
                        println("Producto añadido desde Home: ${producto.nombre}")
                    }
                }
            )
        }
        is Screen.ProductDetail -> {
            ProductDetailScreen(
                producto = currentScreen.producto,
                context = context,
                carritoRepository = carritoRepository,
                onBackClick = {
                    onScreenChange(Screen.Home)
                },
                onAddToCart = { producto ->
                    println("Producto añadido al carrito: ${producto.nombre}")
                }
            )
        }
        is Screen.Catalog -> {
            CatalogScreen(
                productoRepository = productoRepository,
                context = context,
                onBackClick = {
                    onScreenChange(Screen.Home)
                },
                onCategoryClick = { categoria ->
                    selectedCategory.value = categoria
                    onScreenChange(Screen.CategoryProducts(categoria))
                },
                onSearchClick = {
                    // Podemos implementar búsqueda desde catálogo
                },
                onCartClick = onCartClick
            )
        }
        is Screen.CategoryProducts -> {
            CategoryProductsScreen(
                categoria = currentScreen.categoria,
                productoRepository = productoRepository,
                context = context,
                onBackClick = {
                    onScreenChange(Screen.Catalog)
                },
                onProductClick = { producto ->
                    selectedProduct.value = producto
                    onScreenChange(Screen.ProductDetail(producto))
                },
                onAddToCart = { producto -> // ← NUEVO CALLBACK
                    coroutineScope.launch {
                        carritoRepository.agregarProducto(producto, 1)
                        println("Producto añadido desde categoría: ${producto.nombre}")
                    }
                }
            )
        }
        is Screen.Cart -> {
            CartScreen(
                carritoRepository = carritoRepository,
                context = context,
                onBackClick = {
                    onScreenChange(Screen.Home)
                },
                onCheckoutClick = {
                    println("Proceder al pago")
                }
            )
        }
    }
}