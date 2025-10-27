package cl.duoc.level_up_mobile.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.level_up_mobile.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignupViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignupUiState>(SignupUiState.Idle)
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun signUp(email: String, password: String, confirmPassword: String, displayName: String) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || displayName.isBlank()) {
            _uiState.value = SignupUiState.Error("Todos los campos son obligatorios")
            return
        }

        if (password != confirmPassword) {
            _uiState.value = SignupUiState.Error("Las contraseñas no coinciden")
            return
        }

        if (password.length < 6) {
            _uiState.value = SignupUiState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = SignupUiState.Error("Ingresa un email válido")
            return
        }

        _uiState.value = SignupUiState.Loading

        viewModelScope.launch {
            try {
                val user = authRepository.signUp(email, password, displayName)
                if (user != null) {
                    _uiState.value = SignupUiState.Success(user)
                } else {
                    _uiState.value = SignupUiState.Error("Error al crear la cuenta")
                }
            } catch (e: Exception) {
                _uiState.value = SignupUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() {
        _uiState.value = SignupUiState.Idle
    }
}

sealed class SignupUiState {
    object Idle : SignupUiState()
    object Loading : SignupUiState()
    data class Success(val user: cl.duoc.level_up_mobile.model.User?) : SignupUiState()
    data class Error(val message: String) : SignupUiState()
}