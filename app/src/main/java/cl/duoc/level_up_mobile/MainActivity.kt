package cl.duoc.level_up_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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

                // Estado reactivo del usuario actual
                val currentUser by produceState<User?>(
                    initialValue = null,
                    key1 = Unit
                ) {
                    val authStateListener = FirebaseAuth.AuthStateListener { auth ->
                        val firebaseUser = auth.currentUser
                        val user = firebaseUser?.let {
                            User(uid = it.uid, email = it.email ?: "", displayName = it.displayName ?: "")
                        }

                        // ✅ TRANSFERIR CARRITO cuando un usuario se loguea
                        if (firebaseUser != null && value == null) {
                            // Usuario acaba de loguearse (antes era null)
                            scope.launch {
                                carritoRepository.transferirCarritoGuestAUsuario(firebaseUser.uid)
                                carritoRepository.debugUsuarios() // Para verificar
                            }
                        }

                        // ✅ LIMPIAR CARRITO GUEST cuando un usuario hace logout
                        if (firebaseUser == null && value != null) {
                            // Usuario acaba de hacer logout
                            scope.launch {
                                // Opcional: limpiar carrito guest si quieres
                                // carritoRepository.limpiarCarritoGuest()
                            }
                        }

                        value = user
                    }

                    FirebaseAuth.getInstance().addAuthStateListener(authStateListener)

                    awaitDispose {
                        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
                    }
                }

                val isUserLoggedIn = currentUser != null

                // Estado de la pantalla actual - SIEMPRE inicia en Home
                var currentScreen by remember {
                    mutableStateOf<Screen>(Screen.Home)
                }

                MainDrawer(
                    drawerState = drawerState,
                    currentRoute = when (currentScreen) {
                        is Screen.Home -> "inicio"
                        is Screen.Catalog -> "catalogo"
                        is Screen.Cart -> "carrito"
                        is Screen.Login -> "login"
                        else -> "inicio"
                    },
                    currentUser = currentUser,
                    onItemClick = { route ->
                        scope.launch { drawerState.close() }
                        when (route) {
                            "inicio" -> currentScreen = Screen.Home
                            "catalogo" -> currentScreen = Screen.Catalog
                            "carrito" -> currentScreen = Screen.Cart
                            "login" -> currentScreen = Screen.Login
                            "logout" -> {
                                // ✅ Hacer logout
                                FirebaseAuth.getInstance().signOut()
                                // No cambiamos la pantalla, se queda en la actual
                            }
                            "perfil" -> {
                                // Si tienes pantalla de perfil:
                                // currentScreen = Screen.Profile
                            }
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // ✅ SIEMPRE mostrar AppNavigation, el login se maneja internamente
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
                            }
                        )
                    }
                }
            }
        }
    }
}