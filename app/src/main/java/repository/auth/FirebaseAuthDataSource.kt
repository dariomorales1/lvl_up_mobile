package cl.duoc.level_up_mobile.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseAuthDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun signIn(email: String, pass: String): FirebaseUser? =
        suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { cont.resume(it.user) }
                .addOnFailureListener { cont.resume(null) }
        }

    suspend fun signUp(email: String, pass: String): FirebaseUser? =
        suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { cont.resume(it.user) }
                .addOnFailureListener { cont.resume(null) }
        }

    suspend fun sendPasswordReset(email: String): Boolean =
        suspendCancellableCoroutine { cont ->
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
        }

    suspend fun updateProfile(displayName: String?, photoUrl: String?): Boolean =
        suspendCancellableCoroutine { cont ->
            val user = auth.currentUser
            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder().apply {
                    displayName?.let { setDisplayName(it) }
                    photoUrl?.let { setPhotoUri(android.net.Uri.parse(it)) }
                }.build()

                user.updateProfile(profileUpdates)
                    .addOnSuccessListener { cont.resume(true) }
                    .addOnFailureListener { cont.resume(false) }
            } else {
                cont.resume(false)
            }
        }

    fun currentUser(): FirebaseUser? = auth.currentUser
    fun signOut() = auth.signOut()
}