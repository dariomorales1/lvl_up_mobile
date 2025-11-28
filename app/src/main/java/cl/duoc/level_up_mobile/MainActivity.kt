package cl.duoc.level_up_mobile

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

import cl.duoc.level_up_mobile.model.User
import cl.duoc.level_up_mobile.repository.auth.AuthRepository
import cl.duoc.level_up_mobile.repository.carrito.CarritoRepository
import cl.duoc.level_up_mobile.repository.productos.ProductoRepository
import cl.duoc.level_up_mobile.ui.navigation.AppNavigation
import cl.duoc.level_up_mobile.ui.navigation.MainDrawer
import cl.duoc.level_up_mobile.ui.navigation.Screen
import cl.duoc.level_up_mobile.ui.theme.LevelUp_MobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Repos que dependen de Context los creo fuera de Compose
        val carritoRepository = CarritoRepository(this)
        val authRepository = AuthRepository()

        setContent {
            LevelUp_MobileTheme {

                // 🔹 ProductoRepository ahora usa el MS de productos vía RetrofitClient
                val productoRepository = remember { ProductoRepository() }

                val drawerState = rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                // ==============================
                // 🔹 Estado de usuario actual (Firebase + modelo User)
                // ==============================
                val currentUser by produceState<User?>(
                    initialValue = null,
                    key1 = Unit
                ) {
                    val authStateListener = FirebaseAuth.AuthStateListener { auth ->
                        val firebaseUser = auth.currentUser
                        val user = firebaseUser?.let {
                            User(
                                uid = it.uid,
                                email = it.email ?: "",
                                displayName = it.displayName ?: ""
                            )
                        }

                        Log.d(
                            "AuthDebug",
                            "🔄 AuthState: ${firebaseUser?.email ?: "null"}"
                        )

                        // Si el usuario se loguea y antes era guest, transferimos carrito
                        if (firebaseUser != null && value == null) {
                            scope.launch {
                                carritoRepository.transferirCarritoGuestAUsuario(firebaseUser.uid)
                            }
                        }

                        value = user
                    }

                    FirebaseAuth.getInstance().addAuthStateListener(authStateListener)

                    awaitDispose {
                        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
                    }
                }

                // ==============================
                // 🔹 Navegación principal
                // ==============================
                var currentScreen by remember {
                    mutableStateOf<Screen>(Screen.Home)
                }

                var shouldForceLogin by remember { mutableStateOf(false) }

                LaunchedEffect(shouldForceLogin) {
                    if (shouldForceLogin) {
                        currentScreen = Screen.Login
                        shouldForceLogin = false
                    }
                }

                // Función para obtener la ruta actual
                fun getCurrentRoute(): String {
                    return when (currentScreen) {
                        is Screen.Home -> "inicio"
                        is Screen.Catalog -> "catalogo"
                        is Screen.Cart -> "carrito"
                        is Screen.Blog -> "blog"
                        is Screen.Contact -> "contacto"
                        is Screen.Login -> "login"
                        is Screen.Signup -> "signup"
                        is Screen.Profile -> "perfil"
                        is Screen.Address -> "direcciones" // NUEVO
                        else -> "inicio"
                    }
                }

                MainDrawer(
                    drawerState = drawerState,
                    currentRoute = getCurrentRoute(),
                    currentUser = currentUser,
                    onItemClick = { route ->
                        scope.launch { drawerState.close() }

                        when (route) {
                            "inicio" -> currentScreen = Screen.Home
                            "catalogo" -> currentScreen = Screen.Catalog
                            "carrito" -> currentScreen = Screen.Cart
                            "blog" -> currentScreen = Screen.Blog
                            "contacto" -> currentScreen = Screen.Contact
                            "direcciones" -> currentScreen = Screen.Address // NUEVO
                            "login" -> currentScreen = Screen.Login
                            "signup" -> currentScreen = Screen.Signup
                            "perfil" -> currentScreen = Screen.Profile
                            "logout" -> {
                                scope.launch {
                                    authRepository.logout()
                                    currentScreen = Screen.Home
                                    snackbarHostState.showSnackbar(
                                        message = "¡Hasta pronto!",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    },
                    onShowComingSoonMessage = { featureName ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "$featureName estará disponible próximamente",
                                actionLabel = "OK",
                                duration = SnackbarDuration.Short
                            )
                        }
                        Log.d("Navigation", "Función próxima: $featureName")
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            context = this@MainActivity,
                            productoRepository = productoRepository, // 👈 ya apunta al MS de productos
                            carritoRepository = carritoRepository,
                            drawerState = drawerState,
                            currentScreen = currentScreen,
                            onScreenChange = { newScreen ->
                                currentScreen = newScreen
                            },
                            onMenuClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            },
                            onCartClick = {
                                currentScreen = Screen.Cart
                            },
                            currentUser = currentUser,
                            onLoginRequired = {
                                currentScreen = Screen.Login
                            },
                            authRepository = authRepository,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
}