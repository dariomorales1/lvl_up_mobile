// AuthStateManager.kt
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import cl.duoc.level_up_mobile.model.User
import kotlinx.coroutines.channels.awaitClose

object AuthStateManager {
    private val auth = FirebaseAuth.getInstance()

    // Flujo reactivo que emite el usuario actual
    val currentUser: Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            val user = firebaseUser?.let {
                User(uid = it.uid, email = it.email ?: "", displayName = it.displayName ?: "")
            }
            trySend(user)
        }

        auth.addAuthStateListener(authStateListener)

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    // Flujo reactivo para saber si está logueado
    val isUserLoggedIn: Flow<Boolean> = currentUser.map { it != null }

    fun logout() {
        auth.signOut()
    }

    // Función de conveniencia para obtener el usuario actual (no reactiva)
    fun getCurrentUser(): User? {
        return auth.currentUser?.let {
            User(uid = it.uid, email = it.email ?: "", displayName = it.displayName ?: "")
        }
    }
}