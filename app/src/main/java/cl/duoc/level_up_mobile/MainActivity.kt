// En MainActivity.kt - dentro del setContent
val carritoRepository = CarritoRepository(this)

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

        value = user
    }

    FirebaseAuth.getInstance().addAuthStateListener(authStateListener)

    awaitDispose {
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }
}