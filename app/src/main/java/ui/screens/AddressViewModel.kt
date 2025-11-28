package cl.duoc.level_up_mobile.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.level_up_mobile.data.remote.core.RetrofitClient
import cl.duoc.level_up_mobile.data.remote.user.UserApi
import cl.duoc.level_up_mobile.data.remote.user.dto.DireccionRequest
import cl.duoc.level_up_mobile.data.remote.user.dto.DireccionResponse
import cl.duoc.level_up_mobile.session.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddressUiState(
    val loading: Boolean = false,
    val loadingList: Boolean = true,
    val direcciones: List<DireccionResponse> = emptyList(),

    val alias: String = "",
    val calle: String = "",
    val numero: String = "",
    val depto: String = "",
    val ciudad: String = "",
    val region: String = "",
    val pais: String = "",

    val editingId: Long? = null,

    val message: String? = null,
    val error: String? = null
)

class AddressViewModel : ViewModel() {

    private val api = RetrofitClient.retrofit.create(UserApi::class.java)

    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState

    init {
        loadAddresses()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }

    // -------------------- Load list --------------------------
    fun loadAddresses() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(loadingList = true)

                val token = AuthSession.accessToken ?: return@launch
                val res = api.getDirecciones("Bearer $token")

                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        direcciones = res.body() ?: emptyList(),
                        loadingList = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        loadingList = false,
                        error = "No se pudieron cargar las direcciones"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loadingList = false,
                    error = "Error de red"
                )
            }
        }
    }

    // -------------------- Save (create or update) --------------------
    fun saveAddress() {
        viewModelScope.launch {
            val state = _uiState.value

            val req = DireccionRequest(
                alias = state.alias,
                calle = state.calle,
                numero = state.numero,
                depto = state.depto,
                ciudad = state.ciudad,
                region = state.region,
                pais = state.pais
            )

            try {
                _uiState.value = state.copy(loading = true)

                val token = AuthSession.accessToken ?: return@launch
                val bearer = "Bearer $token"

                val response =
                    if (state.editingId == null)
                        api.createDireccion(bearer, req)
                    else
                        api.updateDireccion(state.editingId, bearer, req)

                if (response.isSuccessful) {
                    loadAddresses()
                    resetForm()
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        message = "Dirección guardada"
                    )
                } else {
                    _uiState.value = state.copy(
                        loading = false,
                        error = "Error al guardar dirección"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    loading = false,
                    error = "Error de red"
                )
            }
        }
    }

    // -------------------- Delete --------------------
    fun deleteAddress(id: Long) {
        viewModelScope.launch {
            try {
                val token = AuthSession.accessToken ?: return@launch
                val bearer = "Bearer $token"

                val res = api.deleteDireccion(id, bearer)

                if (res.isSuccessful) {
                    loadAddresses()
                    _uiState.value = _uiState.value.copy(message = "Dirección eliminada")
                } else {
                    _uiState.value = _uiState.value.copy(error = "Error al eliminar dirección")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error de red")
            }
        }
    }

    // -------------------- Load into editor --------------------
    fun loadForEdit(dir: DireccionResponse) {
        _uiState.value = _uiState.value.copy(
            alias = dir.alias ?: "",
            calle = dir.calle ?: "",
            numero = dir.numero ?: "",
            depto = dir.depto ?: "",
            ciudad = dir.ciudad ?: "",
            region = dir.region ?: "",
            pais = dir.pais ?: "",
            editingId = dir.id
        )
    }

    private fun resetForm() {
        _uiState.value = _uiState.value.copy(
            alias = "",
            calle = "",
            numero = "",
            depto = "",
            ciudad = "",
            region = "",
            pais = "",
            editingId = null
        )
    }

    // -------------------- Field handlers --------------------
    fun onAliasChange(v: String) { _uiState.value = _uiState.value.copy(alias = v) }
    fun onCalleChange(v: String) { _uiState.value = _uiState.value.copy(calle = v) }
    fun onNumeroChange(v: String) { _uiState.value = _uiState.value.copy(numero = v) }
    fun onDeptoChange(v: String) { _uiState.value = _uiState.value.copy(depto = v) }
    fun onCiudadChange(v: String) { _uiState.value = _uiState.value.copy(ciudad = v) }
    fun onRegionChange(v: String) { _uiState.value = _uiState.value.copy(region = v) }
    fun onPaisChange(v: String) { _uiState.value = _uiState.value.copy(pais = v) }
}
