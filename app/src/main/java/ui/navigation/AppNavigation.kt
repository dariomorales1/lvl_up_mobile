package cl.duoc.level_up_mobile.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import cl.duoc.level_up_mobile.model.Producto
import cl.duoc.level_up_mobile.model.User
import cl.duoc.level_up_mobile.ui.screens.CatalogScreen
import cl.duoc.level_up_mobile.ui.screens.CategoryProductsScreen
import cl.duoc.level_up_mobile.ui.screens.HomeScreen
import cl.duoc.level_up_mobile.ui.screens.ProductDetailScreen
import cl.duoc.level_up_mobile.ui.screens.CartScreen
import cl.duoc.level_up_mobile.ui.login.LoginScreen
import cl.duoc.level_up_mobile.ui.signup.SignupScreen
import androidx.compose.ui.unit.dp
import cl.duoc.level_up_mobile.ui.screens.BlogScreen
import cl.duoc.level_up_mobile.ui.screens.ContactoScreen
import cl.duoc.level_up_mobile.repository.auth.AuthRepository
import cl.duoc.level_up_mobile.ui.screens.ProfileScreen

sealed class Screen {
    object Home : Screen()
    data class ProductDetail(val producto: Producto) : Screen()
    object Catalog : Screen()
    data class CategoryProducts(val categoria: String) : Screen()
    object Cart : Screen()
    object Login: Screen()
    object Signup: Screen()
    object Blog: Screen()
    object Contact: Screen()
    object Profile: Screen()
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
    onCartClick: () -> Unit,
    currentUser: User?,
    onLoginRequired: () -> Unit,
    authRepository: AuthRepository,
    snackbarHostState: SnackbarHostState
) {
    val selectedProduct = remember { mutableStateOf<Producto?>(null) }
    val selectedCategory = remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val cartItemCount = produceState(
        initialValue = 0,
        key1 = currentUser
    ) {
        carritoRepository.obtenerCarrito().collect { items ->
            val total = items.sumOf { it.cantidad }
            value = total
        }
    }

    LaunchedEffect(currentUser) {
        Log.d("AppNavigation", "👤 Estado usuario en navegación: ${currentUser?.email ?: "NO LOGUEADO"}")
    }

    fun showSnackbar(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
        }
    }

    fun requireLogin(action: () -> Unit) {
        if (currentUser != null) {
            action()
        } else {
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Debes iniciar sesión para continuar",
                    actionLabel = "Iniciar Sesión",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onLoginRequired()
                }
            }
        }
    }

    fun navigateToHome() = onScreenChange(Screen.Home)
    fun navigateToLogin() = onScreenChange(Screen.Login)

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
                            onScreenChange(Screen.ProductDetail(producto))
                        },
                        onCartClick = onCartClick,
                        context = context,
                        cartItemCount = cartItemCount.value,
                        onAddToCart = { producto ->
                            if (currentUser == null) {
                                navigateToLogin()
                                showSnackbar("nicia sesión para añadir productos al carrito")
                            } else {
                                coroutineScope.launch {
                                    carritoRepository.agregarProducto(producto, 1)
                                    showSnackbar("${producto.nombre} añadido al carrito")
                                }
                            }
                        },
                        currentUser = currentUser,
                        onLoginRequired = { navigateToLogin() }
                    )
                }

                is Screen.ProductDetail -> {
                    ProductDetailScreen(
                        producto = currentScreen.producto,
                        context = context,
                        carritoRepository = carritoRepository,
                        onBackClick = { navigateToHome() },
                        onAddToCart = { producto ->
                            if (currentUser == null) {
                                navigateToLogin()
                                showSnackbar("nicia sesión para añadir productos al carrito")
                            } else {
                                coroutineScope.launch {
                                    carritoRepository.agregarProducto(producto, 1)
                                    showSnackbar("${producto.nombre} añadido al carrito")
                                }
                            }
                        }
                    )
                }

                is Screen.CategoryProducts -> {
                    CategoryProductsScreen(
                        categoria = currentScreen.categoria,
                        productoRepository = productoRepository,
                        context = context,
                        onBackClick = { onScreenChange(Screen.Catalog) },
                        onProductClick = { producto ->
                            onScreenChange(Screen.ProductDetail(producto))
                        },
                        onAddToCart = { producto ->
                            coroutineScope.launch {
                                if (currentUser == null) {
                                    navigateToLogin()
                                    showSnackbar("Inicia sesión para añadir productos al carrito")
                                } else {
                                    carritoRepository.agregarProducto(producto, 1)
                                    showSnackbar("${producto.nombre} añadido al carrito")
                                }
                            }
                        },
                        onCartClick = onCartClick,
                        cartItemCount = cartItemCount.value,
                        currentUser = currentUser,
                        onLoginRequired = { navigateToLogin() }
                    )
                }

                is Screen.Catalog -> {
                    CatalogScreen(
                        productoRepository = productoRepository,
                        context = context,
                        onBackClick = { navigateToHome() },
                        onCategoryClick = { categoria ->
                            onScreenChange(Screen.CategoryProducts(categoria))
                        },
                        onSearchClick = {},
                        onCartClick = onCartClick,
                        cartItemCount = cartItemCount.value
                    )
                }

                is Screen.Cart -> {
                    CartScreen(
                        carritoRepository = carritoRepository,
                        context = context,
                        onBackClick = { navigateToHome() },
                        onCheckoutClick = {
                            requireLogin {
                                showSnackbar("Procediendo al pago para ${currentUser?.email}...")
                            }
                        },
                        currentUser = currentUser,
                        onLoginRequired = { navigateToLogin() }
                    )
                }

                is Screen.Login -> {
                    LoginScreen(
                        onBack = { navigateToHome() },
                        onLoginSuccess = {
                            navigateToHome()
                            showSnackbar("¡Bienvenido ${currentUser?.email}!")
                        },
                        currentUser = currentUser,
                        onNavigateToSignup = { onScreenChange(Screen.Signup) }
                    )
                }

                is Screen.Signup -> {
                    SignupScreen(
                        onSignupSuccess = {
                            navigateToHome()
                            showSnackbar("¡Cuenta creada exitosamente! Bienvenido ${currentUser?.email}")
                        },
                        onNavigateToLogin = { onScreenChange(Screen.Login) },
                        authRepository = authRepository
                    )
                }

                is Screen.Blog -> {
                    BlogScreen(
                        onBackClick = { navigateToHome() }
                    )
                }

                is Screen.Contact -> {
                    ContactoScreen(
                        onBackClick = { navigateToHome() }
                    )
                }

                is Screen.Profile -> {
                    ProfileScreen(
                        context = context,
                        currentUser = currentUser,
                        onBackClick = { navigateToHome() },
                        onShowSnackbar = { message -> showSnackbar(message) }
                    )
                }
            }
        }
    }
}