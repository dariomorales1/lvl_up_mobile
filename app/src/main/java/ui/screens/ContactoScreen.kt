package cl.duoc.level_up_mobile.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import cl.duoc.level_up_mobile.ui.navigation.Contacto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactoScreen(onBackClick: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // 👈 usamos coroutine scope

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contáctanos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Contacto(
                innerPadding = innerPadding,
                onCancel = {},
                onSend = { nombre, correo, _ ->
                    scope.launch {
                        snackbarHostState.showSnackbar("¡Gracias $nombre! Te responderemos a $correo.")
                    }
                },
                onValidationError = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            )
        }
    }
}


