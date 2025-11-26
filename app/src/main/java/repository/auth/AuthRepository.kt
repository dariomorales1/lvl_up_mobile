package cl.duoc.level_up_mobile.repository.auth

import cl.duoc.level_up_mobile.data.remote.auth.AuthApi
import cl.duoc.level_up_mobile.data.remote.auth.dto.AuthResponse
import cl.duoc.level_up_mobile.data.remote.auth.dto.LoginRequest
import cl.duoc.level_up_mobile.data.remote.auth.dto.RefreshRequest
import cl.duoc.level_up_mobile.data.remote.core.RetrofitClient
import cl.duoc.level_up_mobile.data.remote.user.UserApi
import cl.duoc.level_up_mobile.data.remote.user.dto.UsuarioPublicRequest
import cl.duoc.level_up_mobile.model.User
import cl.duoc.level_up_mobile.session.AuthSession
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class AuthRepository(
    private val ds: FirebaseAuthDataSource = FirebaseAuthDataSource(),
    private val authApi: AuthApi = RetrofitClient.retrofit.create(AuthApi::class.java),
    private val userApi: UserApi = RetrofitClient.retrofit.create(UserApi::class.java)
) {

    fun getAccessToken(): String? = AuthSession.accessToken
    fun getRefreshToken(): String? = AuthSession.refreshToken

    // -------- LOGIN --------
    suspend fun login(email: String, pass: String): User? {
        val fu = ds.signIn(email, pass) ?: return null

        val idToken = fu.getIdTokenOrNull() ?: return null

        val response = try {
            authApi.login(LoginRequest(firebaseIdToken = idToken))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        if (!response.isSuccessful) return null

        val body: AuthResponse = response.body() ?: return null

        val access = body.accessToken
        val refresh = body.refreshToken

        if (access.isNullOrBlank() || refresh.isNullOrBlank()) return null

        AuthSession.setTokens(access, refresh)

        val emailFinal = body.email ?: fu.email
        val displayNameFinal = fu.displayName

        return User(
            uid = body.userId ?: fu.uid,
            email = emailFinal,
            displayName = displayNameFinal
        )
    }

    // -------- SIGNUP + USER SERVICE --------
    suspend fun signUp(
        email: String,
        pass: String,
        displayName: String? = null,
        birthDate: String? = null
    ): User? {
        // 1) Crear usuario en Firebase
        val fu = ds.signUp(email, pass) ?: return null

        // 2) Actualizar displayName en Firebase si se envió
        displayName?.let { name ->
            updateUserProfile(name, null)
        }

        // Email y nombre finales
        val finalEmail = fu.email ?: email
        val finalNombre = displayName ?: fu.displayName ?: finalEmail.substringBefore("@")
        val finalFechaNacimiento = birthDate

        // Rol según dominio del correo (igual que en React)
        val rol = if (finalEmail.endsWith("@levelup.ddns.net")) "ADMIN" else "USER"

        // 3) Registrar usuario en User Service (endpoint público)
        val userRequest = UsuarioPublicRequest(
            firebaseUid = fu.uid,
            email = finalEmail,
            nombre = finalNombre,
            fechaNacimiento = finalFechaNacimiento,
            avatarUrl = "https://mi-avatar.com/avatar.png",
            rol = rol
        )

        try {
            userApi.registerPublic(userRequest)
        } catch (e: Exception) {
            // No rompemos el signup si falla el User Service, solo logueamos
            e.printStackTrace()
        }

        // 4) Obtener ID Token y loguear contra Auth Service para tener JWT propios
        val idToken = fu.getIdTokenOrNull()

        if (idToken != null) {
            try {
                val authResponse = authApi.login(LoginRequest(firebaseIdToken = idToken))
                if (authResponse.isSuccessful) {
                    val body: AuthResponse? = authResponse.body()
                    val access = body?.accessToken
                    val refresh = body?.refreshToken

                    if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                        AuthSession.setTokens(access, refresh)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 5) Devolvemos User de dominio
        return User(
            uid = fu.uid,
            email = finalEmail,
            displayName = finalNombre
        )
    }

    suspend fun sendPasswordReset(email: String): Boolean {
        return ds.sendPasswordReset(email)
    }

    suspend fun updateUserProfile(displayName: String?, photoUrl: String?): Boolean {
        return ds.updateProfile(displayName, photoUrl)
    }

    // -------- LOGOUT --------
    suspend fun logout() {
        val refresh = AuthSession.refreshToken
        if (refresh != null) {
            try {
                withContext(Dispatchers.IO) {
                    authApi.logout(RefreshRequest(refreshToken = refresh))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        AuthSession.clear()
        ds.signOut()
    }

    fun currentUser(): User? = ds.currentUser()?.let {
        User(it.uid, it.email, it.displayName)
    }
}

private suspend fun FirebaseUser.getIdTokenOrNull(): String? =
    suspendCancellableCoroutine { cont ->
        this.getIdToken(true)
            .addOnSuccessListener { result ->
                cont.resume(result.token)
            }
            .addOnFailureListener {
                cont.resume(null)
            }
    }
