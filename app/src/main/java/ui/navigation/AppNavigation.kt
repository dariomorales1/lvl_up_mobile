package cl.duoc.level_up_mobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import cl.duoc.level_up_mobile.model.Producto
import cl.duoc.level_up_mobile.ui.screens.CatalogScreen
import cl.duoc.level_up_mobile.ui.screens.CategoryProductsScreen
import cl.duoc.level_up_mobile.ui.screens.HomeScreen
import cl.duoc.level_up_mobile.ui.screens.ProductDetailScreen
import cl.duoc.level_up_mobile.ui.screens.CartScreen
import androidx.compose.ui.unit.dp

sealed class Screen {
    object Home : Screen()
    data class ProductDetail(val producto: Producto) : Screen()
    object Catalog : Screen()
    data class CategoryProducts(val categoria: String) : Screen()
    object Cart : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para el contador del carrito
    val cartItemCount = produceState(initialValue = 0) {
        carritoRepository.obtenerCarrito().collect { items ->
            value = items.sumOf { it.cantidad }
        }
    }
    // Función para mostrar mensajes
    fun showSnackbar(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
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
                        cartItemCount = cartItemCount.value,
                        onAddToCart = { producto ->
                            coroutineScope.launch {
                                carritoRepository.agregarProducto(producto, 1)
                                showSnackbar("✅ ${producto.nombre} añadido al carrito")
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
                            coroutineScope.launch {
                                carritoRepository.agregarProducto(producto, 1)
                                showSnackbar("✅ ${producto.nombre} añadido al carrito")
                            }
                        }
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
                        onAddToCart = { producto ->
                            coroutineScope.launch {
                                carritoRepository.agregarProducto(producto, 1)
                                showSnackbar("✅ ${producto.nombre} añadido al carrito")
                            }
                        },
                        onCartClick = onCartClick,
                        cartItemCount = cartItemCount.value
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
                        onCartClick = onCartClick,
                        cartItemCount = cartItemCount.value
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
    }
}