package cl.duoc.level_up_mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.level_up_mobile.data.remote.user.dto.DireccionRequest
import cl.duoc.level_up_mobile.data.remote.user.dto.DireccionResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    onBackClick: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    vm: AddressViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(state.message, state.error) {
        state.message?.let {
            onShowSnackbar(it)
            vm.clearMessages()
        }
        state.error?.let {
            onShowSnackbar(it)
            vm.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Direcciones") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(20.dp)
                .fillMaxSize()
        ) {

            // ------------------ FORMULARIO --------------------
            Text("Nueva Dirección", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.alias,
                onValueChange = vm::onAliasChange,
                label = { Text("Alias (Ej: Casa, Trabajo)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.calle,
                onValueChange = vm::onCalleChange,
                label = { Text("Calle") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.numero,
                    onValueChange = vm::onNumeroChange,
                    label = { Text("Número") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.depto,
                    onValueChange = vm::onDeptoChange,
                    label = { Text("Depto (opcional)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.ciudad,
                onValueChange = vm::onCiudadChange,
                label = { Text("Ciudad / Comuna") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.region,
                onValueChange = vm::onRegionChange,
                label = { Text("Región") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.pais,
                onValueChange = vm::onPaisChange,
                label = { Text("País") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { vm.saveAddress() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.loading
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(6.dp))
                Text(if (state.editingId == null) "Guardar dirección" else "Actualizar dirección")
            }

            Spacer(Modifier.height(20.dp))

            Divider()

            Spacer(Modifier.height(20.dp))

            // ------------------ LISTA DE DIRECCIONES --------------------
            Text("Tus Direcciones", style = MaterialTheme.typography.titleMedium)

            if (state.loadingList) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.direcciones) { dir ->
                        AddressItem(
                            direccion = dir,
                            onEdit = { vm.loadForEdit(dir) },
                            onDelete = { vm.deleteAddress(dir.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddressItem(
    direccion: DireccionResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Home, null)
                Spacer(Modifier.width(8.dp))
                Text("${direccion.alias ?: "Sin alias"}", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(6.dp))
            Text("${direccion.calle} ${direccion.numero} ${direccion.depto ?: ""}")
            Text("${direccion.ciudad}, ${direccion.region}, ${direccion.pais}")

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar")
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}
