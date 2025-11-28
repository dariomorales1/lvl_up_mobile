package cl.duoc.level_up_mobile.ui.screens.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.level_up_mobile.data.remote.user.dto.DireccionRequest
import cl.duoc.level_up_mobile.data.remote.user.dto.DireccionResponse
import cl.duoc.level_up_mobile.repository.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddressUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,

    val addresses: List<DireccionResponse> = emptyList(),

    // Form fields
    val editingId: Long? = null,
    val alias: String = "",
    val calle: String = "",
    val numero: String = "",
    val depto: String = "",
    val ciudad: String = "",
    val region: String = "",
    val pais: String = "Chile"
)

class AddressViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            val addresses = userRepository.getMyAddresses()
            _uiState.update {
                it.copy(
                    loading = false,
                    addresses = addresses,
                    error = if (addresses.isEmpty()) "No hay direcciones guardadas" else null
                )
            }
        }
    }

    fun onAliasChange(value: String) {
        _uiState.update { it.copy(alias = value) }
    }

    fun onCalleChange(value: String) {
        _uiState.update { it.copy(calle = value) }
    }

    fun onNumeroChange(value: String) {
        _uiState.update { it.copy(numero = value) }
    }

    fun onDeptoChange(value: String) {
        _uiState.update { it.copy(depto = value) }
    }

    fun onCiudadChange(value: String) {
        _uiState.update { it.copy(ciudad = value) }
    }

    fun onRegionChange(value: String) {
        _uiState.update { it.copy(region = value) }
    }

    fun onPaisChange(value: String) {
        _uiState.update { it.copy(pais = value) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    fun resetForm() {
        _uiState.update {
            it.copy(
                editingId = null,
                alias = "",
                calle = "",
                numero = "",
                depto = "",
                ciudad = "",
                region = "",
                pais = "Chile"
            )
        }
    }

    fun selectAddressForEdit(address: DireccionResponse) {
        _uiState.update {
            it.copy(
                editingId = address.id,
                alias = address.alias ?: "",
                calle = address.calle ?: "",
                numero = address.numero ?: "",
                depto = address.depto ?: "",
                ciudad = address.ciudad ?: "",
                region = address.region ?: "",
                pais = address.pais ?: "Chile"
            )
        }
    }

    fun saveAddress() {
        val state = _uiState.value

        // Validación básica
        if (state.calle.isBlank() || state.numero.isBlank() || state.ciudad.isBlank()) {
            _uiState.update { it.copy(error = "Completa al menos Calle, Número y Ciudad") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, message = null) }

            val request = DireccionRequest(
                alias = state.alias.ifBlank { null },
                calle = state.calle,
                numero = state.numero,
                depto = state.depto.ifBlank { null },
                ciudad = state.ciudad,
                region = state.region.ifBlank { null },
                pais = state.pais
            )

            val result = if (state.editingId != null) {
                userRepository.updateAddress(state.editingId, request)
            } else {
                userRepository.createAddress(request)
            }

            if (result != null) {
                loadAddresses() // Recargar lista
                resetForm()
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = if (state.editingId != null)
                            "Dirección actualizada correctamente"
                        else
                            "Dirección guardada correctamente"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = "Error al ${if (state.editingId != null) "actualizar" else "guardar"} la dirección"
                    )
                }
            }
        }
    }

    fun deleteAddress(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, message = null) }

            val success = userRepository.deleteAddress(id)
            if (success) {
                loadAddresses()
                // Si estábamos editando esta dirección, resetear form
                if (_uiState.value.editingId == id) {
                    resetForm()
                }
                _uiState.update {
                    it.copy(
                        loading = false,
                        message = "Dirección eliminada"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = "Error al eliminar la dirección"
                    )
                }
            }
        }
    }
}