package cl.duoc.level_up_mobile.ui.screens.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    onBackClick: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val vm: AddressViewModel = viewModel()
    val state by vm.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Mostrar mensajes y errores
    LaunchedEffect(state.message, state.error) {
        state.message?.let {
            onShowSnackbar(it)
            vm.clearMessage()
        }
        state.error?.let {
            onShowSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Direcciones") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // SECCIÓN: FORMULARIO
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        if (state.editingId != null) "Editar dirección" else "Nueva dirección",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Alias
                    OutlinedTextField(
                        value = state.alias,
                        onValueChange = vm::onAliasChange,
                        label = { Text("Alias (opcional)") },
                        placeholder = { Text("Casa, Trabajo, etc.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Calle y Número
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.calle,
                            onValueChange = vm::onCalleChange,
                            label = { Text("Calle *") },
                            placeholder = { Text("Calle principal") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.numero,
                            onValueChange = vm::onNumeroChange,
                            label = { Text("Número *") },
                            placeholder = { Text("123") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Departamento
                    OutlinedTextField(
                        value = state.depto,
                        onValueChange = vm::onDeptoChange,
                        label = { Text("Departamento / Casa") },
                        placeholder = { Text("Depto 304, Casa 2, etc.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Ciudad, Región, País
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.ciudad,
                            onValueChange = vm::onCiudadChange,
                            label = { Text("Ciudad *") },
                            placeholder = { Text("Santiago") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.region,
                            onValueChange = vm::onRegionChange,
                            label = { Text("Región") },
                            placeholder = { Text("RM") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.pais,
                            onValueChange = vm::onPaisChange,
                            label = { Text("País") },
                            placeholder = { Text("Chile") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { vm.saveAddress() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            enabled = !state.loading
                        ) {
                            Text(if (state.editingId != null) "Actualizar dirección" else "Guardar dirección")
                        }

                        if (state.editingId != null) {
                            OutlinedButton(
                                onClick = { vm.resetForm() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                enabled = !state.loading
                            ) {
                                Text("Cancelar")
                            }
                        }
                    }
                }
            }

            // SECCIÓN: LISTA DE DIRECCIONES
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "Direcciones guardadas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    when {
                        state.loading && state.addresses.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        state.addresses.isEmpty() -> {
                            Text(
                                "Aún no tienes direcciones guardadas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        else -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                state.addresses.forEach { address ->
                                    AddressItem(
                                        address = address,
                                        isSelected = state.editingId == address.id,
                                        onEditClick = { vm.selectAddressForEdit(address) },
                                        onDeleteClick = {
                                            scope.launch {
                                                vm.deleteAddress(address.id ?: return@launch)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddressItem(
    address: cl.duoc.level_up_mobile.data.remote.user.dto.DireccionResponse,
    isSelected: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    address.alias ?: "Sin alias",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${address.calle} ${address.numero}${address.depto?.let { ", $it" } ?: ""}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${address.ciudad}${address.region?.let { ", $it" } ?: ""} - ${address.pais}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}