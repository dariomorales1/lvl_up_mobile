package cl.duoc.level_up_mobile

import android.os.Bundle
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cl.duoc.level_up_mobile.repository.carrito.CarritoRepository
import cl.duoc.level_up_mobile.repository.productos.ProductoRepository
import cl.duoc.level_up_mobile.ui.navigation.AppNavigation
import cl.duoc.level_up_mobile.ui.navigation.MainDrawer
import cl.duoc.level_up_mobile.ui.navigation.Screen
import cl.duoc.level_up_mobile.ui.theme.LevelUp_MobileTheme
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import cl.duoc.level_up_mobile.model.User
import android.util.Log
import cl.duoc.level_up_mobile.repository.auth.AuthRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val productoRepository = ProductoRepository(this)
        val carritoRepository = CarritoRepository(this)
        val authRepository = AuthRepository()

        setContent {
            LevelUp_MobileTheme {
                val drawerState = rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                val currentUser by produceState<User?>(
                    initialValue = null,
                    key1 = Unit
                ) {
                    val authStateListener = FirebaseAuth.AuthStateListener { auth ->
                        val firebaseUser = auth.currentUser
                        val user = firebaseUser?.let {
                            User(uid = it.uid, email = it.email ?: "", displayName = it.displayName ?: "")
                        }

                        Log.d("AuthDebug", "🔄 AuthState: ${firebaseUser?.email ?: "null"}")

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

                var currentScreen by remember {
                    mutableStateOf<Screen>(Screen.Home)
                }

                // Controlar cuándo forzar el login
                var shouldForceLogin by remember { mutableStateOf(false) }

                LaunchedEffect(shouldForceLogin) {
                    if (shouldForceLogin) {
                        currentScreen = Screen.Login
                        shouldForceLogin = false
                    }
                }

                MainDrawer(
                    drawerState = drawerState,
                    currentRoute = when (currentScreen) {
                        is Screen.Home -> "inicio"
                        is Screen.Catalog -> "catalogo"
                        is Screen.Cart -> "carrito"
                        is Screen.Blog -> "blog"
                        is Screen.Contact -> "contacto"
                        is Screen.Login -> "login"
                        is Screen.Signup -> "signup"
                        else -> "inicio"
                    },
                    currentUser = currentUser,
                    onItemClick = { route ->
                        scope.launch { drawerState.close() }

                        when (route) {
                            "inicio" -> currentScreen = Screen.Home
                            "catalogo" -> currentScreen = Screen.Catalog
                            "carrito" -> currentScreen = Screen.Cart
                            "blog" -> currentScreen = Screen.Blog
                            "contacto" -> currentScreen = Screen.Contact
                            "login" ->  currentScreen = Screen.Login
                            "signup" -> currentScreen = Screen.Signup
                            "perfil" -> currentScreen = Screen.Profile
                            "logout" -> {
                                FirebaseAuth.getInstance().signOut()
                                currentScreen = Screen.Home
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
                            productoRepository = productoRepository,
                            carritoRepository = carritoRepository,
                            drawerState = drawerState,
                            currentScreen = currentScreen,
                            onScreenChange = { newScreen ->
                                currentScreen = newScreen
                            },
                            onMenuClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
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