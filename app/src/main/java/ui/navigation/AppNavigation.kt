package cl.duoc.level_up_mobile.ui.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.level_up_mobile.model.Producto
import cl.duoc.level_up_mobile.model.User
import cl.duoc.level_up_mobile.repository.auth.AuthRepository
import cl.duoc.level_up_mobile.repository.carrito.CartRepositoryRemote
import cl.duoc.level_up_mobile.repository.productos.ProductoRepository
import cl.duoc.level_up_mobile.ui.screens.BlogScreen
import cl.duoc.level_up_mobile.ui.screens.CartScreen
import cl.duoc.level_up_mobile.ui.screens.CatalogScreen
import cl.duoc.level_up_mobile.ui.screens.CategoryProductsScreen
import cl.duoc.level_up_mobile.ui.screens.ContactoScreen
import cl.duoc.level_up_mobile.ui.screens.HomeScreen
import cl.duoc.level_up_mobile.ui.login.LoginScreen
import cl.duoc.level_up_mobile.ui.screens.ProductDetailScreen
import cl.duoc.level_up_mobile.ui.screens.ProfileScreen
import cl.duoc.level_up_mobile.ui.signup.SignupScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import cl.duoc.level_up_mobile.ui.screens.address.AddressScreen


// ===========================
// 🔹 Definición de pantallas
// ===========================
sealed class Screen {
    object Home : Screen()
    data class ProductDetail(val producto: Producto) : Screen()
    object Catalog : Screen()
    data class CategoryProducts(val categoria: String) : Screen()
    object Cart : Screen()
    object Login : Screen()
    object Signup : Screen()
    object Blog : Screen()
    object Contact : Screen()
    object Profile : Screen()

    object Address : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    context: Context,
    productoRepository: ProductoRepository,
    cartRepository: CartRepositoryRemote,          // 👈 SOLO backend carrito
    drawerState: DrawerState,
    currentScreen: Screen = Screen.Home,
    onScreenChange: (Screen) -> Unit = {},
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    currentUser: User?,
    onLoginRequired: () -> Unit,
    authRepository: AuthRepository,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    // Para forzar refresco de contador del carrito cuando se agrega / quita
    val cartVersion = remember { mutableStateOf(0) }

    // ===========================
    // 🔹 Contador de items carrito
    // ===========================
    val cartItemCount = produceState(
        initialValue = 0,
        key1 = currentUser,
        key2 = cartVersion.value
    ) {
        val userId = currentUser?.uid
        if (userId != null) {
            try {
                val remoteCart = cartRepository.getUserCart(userId)
                value = remoteCart?.items?.sumOf { it.quantity } ?: 0
            } catch (e: Exception) {
                Log.e("AppNavigation", "Error cargando carrito remoto", e)
                value = 0
            }
        } else {
            value = 0
        }
    }

    LaunchedEffect(currentUser) {
        Log.d(
            "AppNavigation",
            "👤 Estado usuario en navegación: ${currentUser?.email ?: "NO LOGUEADO"}"
        )
    }

    fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        customDurationMillis: Long? = null
    ) {
        coroutineScope.launch {
            if (customDurationMillis != null) {
                val job = launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = actionLabel,
                        duration = SnackbarDuration.Indefinite
                    )
                }
                delay(customDurationMillis)
                job.cancel()
            } else {
                snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel,
                    duration = SnackbarDuration.Short
                )
            }
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
                    duration = SnackbarDuration.Short
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

                // ================= HOME =================
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
                                showSnackbar("Inicia sesión para añadir productos al carrito")
                            } else {
                                coroutineScope.launch {
                                    val userId = currentUser.uid ?: return@launch
                                    val updatedCart = cartRepository.addItemToUserCart(
                                        userId = userId,
                                        productId = producto.codigo,
                                        quantity = 1
                                    )
                                    if (updatedCart != null) {
                                        cartVersion.value++
                                        showSnackbar(
                                            "${producto.nombre} añadido al carrito",
                                            customDurationMillis = 1000L
                                        )
                                    } else {
                                        showSnackbar("No se pudo añadir el producto al carrito")
                                    }
                                }
                            }
                        },
                        currentUser = currentUser,
                        onLoginRequired = { navigateToLogin() }
                    )
                }

                // ============== DETALLE PRODUCTO ==============
                is Screen.ProductDetail -> {
                    ProductDetailScreen(
                        producto = currentScreen.producto,
                        context = context,
                        onBackClick = { navigateToHome() },
                        onAddToCart = { producto ->
                            if (currentUser == null) {
                                navigateToLogin()
                                showSnackbar("Inicia sesión para añadir productos al carrito")
                            } else {
                                coroutineScope.launch {
                                    val userId = currentUser.uid ?: return@launch
                                    val updatedCart = cartRepository.addItemToUserCart(
                                        userId = userId,
                                        productId = producto.codigo,
                                        quantity = 1
                                    )
                                    if (updatedCart != null) {
                                        cartVersion.value++
                                        showSnackbar(
                                            "${producto.nombre} añadido al carrito",
                                            customDurationMillis = 1000L
                                        )
                                    } else {
                                        showSnackbar("No se pudo añadir el producto al carrito")
                                    }
                                }
                            }
                        }
                    )
                }

                // ============== CATÁLOGO COMPLETO ==============
                is Screen.Catalog -> {
                    CatalogScreen(
                        productoRepository = productoRepository,
                        context = context,
                        onBackClick = { navigateToHome() },
                        onCategoryClick = { categoria ->
                            onScreenChange(Screen.CategoryProducts(categoria))
                        },
                        onSearchClick = { /* TODO: búsqueda global */ },
                        onCartClick = onCartClick,
                        cartItemCount = cartItemCount.value
                    )
                }

                // =========== PRODUCTOS POR CATEGORÍA ===========
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
                                    val userId = currentUser.uid ?: return@launch
                                    val updatedCart = cartRepository.addItemToUserCart(
                                        userId = userId,
                                        productId = producto.codigo,
                                        quantity = 1
                                    )
                                    if (updatedCart != null) {
                                        cartVersion.value++
                                        showSnackbar(
                                            "${producto.nombre} añadido al carrito",
                                            customDurationMillis = 1000L
                                        )
                                    } else {
                                        showSnackbar("No se pudo añadir el producto al carrito")
                                    }
                                }
                            }
                        },
                        onCartClick = onCartClick,
                        cartItemCount = cartItemCount.value,
                        currentUser = currentUser,
                        onLoginRequired = { navigateToLogin() }
                    )
                }

                // ================= CARRITO =================
                is Screen.Cart -> {
                    CartScreen(
                        cartRepository = cartRepository,
                        currentUser = currentUser,
                        onBackClick = { navigateToHome() },
                        onCheckoutClick = {
                            requireLogin {
                                showSnackbar("Procediendo al pago para ${currentUser?.email}...")
                            }
                        },
                        onLoginRequired = { navigateToLogin() }
                    )
                }

                // ================= BLOG =================
                is Screen.Blog -> {
                    BlogScreen(
                        onBackClick = { navigateToHome() }
                    )
                }

                // ================= CONTACTO =================
                is Screen.Contact -> {
                    ContactoScreen(
                        onBackClick = { navigateToHome() }
                    )
                }

                // ================= LOGIN =================
                is Screen.Login -> {
                    LoginScreen(
                        onBack = { navigateToHome() },
                        onLoginSuccess = {
                            navigateToHome()
                            showSnackbar("¡Bienvenido ${currentUser?.email ?: ""}!")
                        },
                        currentUser = currentUser,
                        onNavigateToSignup = { onScreenChange(Screen.Signup) }
                    )
                }

                // ================= SIGNUP =================
                is Screen.Signup -> {
                    SignupScreen(
                        onSignupSuccess = {
                            navigateToHome()
                            showSnackbar("¡Cuenta creada exitosamente!")
                        },
                        onNavigateToLogin = { onScreenChange(Screen.Login) },
                        authRepository = authRepository
                    )
                }

                // ================= PERFIL =================
                is Screen.Profile -> {
                    ProfileScreen(
                        context = context,
                        currentUser = currentUser,
                        onBackClick = { navigateToHome() },
                        onShowSnackbar = { message -> showSnackbar(message) }
                    )
                }

                // NUEVA PANTALLA DE DIRECCIONES
                is Screen.Address -> {
                    AddressScreen(
                        onBackClick = { navigateToHome() },
                        onShowSnackbar = { message -> showSnackbar(message) }
                    )
                }
            }
        }
    }
}
