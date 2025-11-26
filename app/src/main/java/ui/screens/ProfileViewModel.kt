package cl.duoc.level_up_mobile.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.level_up_mobile.data.remote.user.dto.UsuarioRequest
import cl.duoc.level_up_mobile.data.remote.user.dto.UsuarioResponse
import cl.duoc.level_up_mobile.repository.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.io.File

data class ProfileUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,

    val userId: String? = null,

    val nombre: String = "",
    val email: String = "",
    val fechaNacimiento: String = "",
    val avatarUrl: String? = null,

    val direccion: String = "",
    val comuna: String = "",
    val region: String = "",
    val pais: String = ""
)

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var rawUser: UsuarioResponse? = null

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, message = null) }

            val user = userRepository.getCurrentUser()
            if (user == null || user.id == null) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = "No se pudo cargar el perfil"
                    )
                }
                return@launch
            }

            rawUser = user

            val addresses = userRepository.getMyAddresses()
            val first = pickOldestAddress(addresses)

            _uiState.update {
                it.copy(
                    loading = false,
                    userId = user.id,
                    nombre = user.nombre ?: "",
                    email = user.email ?: "",
                    fechaNacimiento = user.fechaNacimiento ?: "",
                    avatarUrl = user.avatarUrl,
                    direccion = if (first != null) {
                        buildString {
                            append(first.calle.orEmpty())
                            if (!first.numero.isNullOrBlank()) {
                                append(" ")
                                append(first.numero)
                            }
                            if (!first.depto.isNullOrBlank()) {
                                append(", ")
                                append(first.depto)
                            }
                        }
                    } else "",
                    comuna = first?.ciudad ?: "",
                    region = first?.region ?: "",
                    pais = first?.pais ?: ""
                )
            }
        }
    }

    private fun pickOldestAddress(list: List<cl.duoc.level_up_mobile.data.remote.user.dto.DireccionResponse>):
            cl.duoc.level_up_mobile.data.remote.user.dto.DireccionResponse? {
        if (list.isEmpty()) return null
        return list.minByOrNull { dir ->
            parseDateOrEpoch(dir.creadoEn ?: dir.actualizadoEn)
        }
    }

    private fun parseDateOrEpoch(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return Long.MAX_VALUE
        return try {
            val odt = OffsetDateTime.parse(dateStr)
            odt.toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            try {
                val inst = Instant.parse(dateStr)
                inst.toEpochMilli()
            } catch (e2: Exception) {
                Long.MAX_VALUE
            }
        }
    }

    fun onNombreChange(v: String) {
        _uiState.update { it.copy(nombre = v) }
    }

    fun onFechaNacimientoChange(v: String) {
        _uiState.update { it.copy(fechaNacimiento = v) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    fun saveProfile() {
        val state = _uiState.value
        val userId = state.userId ?: return

        if (state.nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, message = null) }

            val current = rawUser
            val request = UsuarioRequest(
                email = state.email,
                nombre = state.nombre,
                fechaNacimiento = state.fechaNacimiento.ifBlank { null },
                avatarUrl = state.avatarUrl ?: current?.avatarUrl,
                rol = current?.rol ?: "USER",
                activo = current?.activo ?: true
            )

            val updated = userRepository.updateUser(userId, request)
            if (updated != null) {
                rawUser = updated
                _uiState.update {
                    it.copy(
                        loading = false,
                        nombre = updated.nombre ?: it.nombre,
                        fechaNacimiento = updated.fechaNacimiento ?: it.fechaNacimiento,
                        email = updated.email ?: it.email,
                        avatarUrl = updated.avatarUrl ?: it.avatarUrl,
                        message = "Perfil actualizado correctamente"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = "Error al actualizar el perfil"
                    )
                }
            }
        }
    }

    fun reload() {
        loadProfile()
    }

    suspend fun uploadAvatar(file: File) {
        val state = _uiState.value
        val userId = state.userId ?: return

        _uiState.update { it.copy(loading = true, error = null, message = null) }

        val updated = userRepository.uploadAvatar(userId, file)
        if (updated != null) {
            rawUser = updated
            _uiState.update {
                it.copy(
                    loading = false,
                    avatarUrl = updated.avatarUrl ?: it.avatarUrl,
                    message = "Avatar actualizado correctamente"
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    loading = false,
                    error = "Error al subir avatar"
                )
            }
        }
    }

    fun deleteAvatar() {
        val state = _uiState.value
        val userId = state.userId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, message = null) }

            val updated = userRepository.deleteAvatar(userId)
            if (updated != null) {
                rawUser = updated
                _uiState.update {
                    it.copy(
                        loading = false,
                        avatarUrl = updated.avatarUrl ?: "",
                        message = "Avatar eliminado correctamente"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = "Error al eliminar avatar"
                    )
                }
            }
        }
    }
}
