package cl.duoc.level_up_mobile.model

data class User(
    val uid: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false
)