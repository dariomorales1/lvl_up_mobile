package cl.duoc.level_up_mobile.repository.auth

import cl.duoc.level_up_mobile.model.User
import com.google.firebase.auth.UserProfileChangeRequest

class AuthRepository(
    private val ds: FirebaseAuthDataSource = FirebaseAuthDataSource()
) {
    suspend fun login(email: String, pass: String): User? {
        val fu = ds.signIn(email, pass) ?: return null
        return User(uid = fu.uid, email = fu.email, displayName = fu.displayName)
    }

    suspend fun signUp(email: String, pass: String, displayName: String? = null): User? {
        val fu = ds.signUp(email, pass) ?: return null

        displayName?.let { name ->
            updateUserProfile(name, null)
        }

        return User(uid = fu.uid, email = fu.email, displayName = displayName ?: fu.displayName)
    }

    suspend fun sendPasswordReset(email: String): Boolean {
        return ds.sendPasswordReset(email)
    }

    suspend fun updateUserProfile(displayName: String?, photoUrl: String?): Boolean {
        return ds.updateProfile(displayName, photoUrl)
    }

    fun logout() = ds.signOut()
    fun currentUser(): User? = ds.currentUser()?.let {
        User(it.uid, it.email, it.displayName)
    }
}