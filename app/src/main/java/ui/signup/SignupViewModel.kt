package cl.duoc.level_up_mobile.ui.signup

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.level_up_mobile.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeParseException

class SignupViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignupUiState>(SignupUiState.Idle)
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun signUp(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String,
        birthDate: String,
        acceptedTerms: Boolean
    ) {
        // Validaciones similares al frontend React

        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() ||
            displayName.isBlank() || birthDate.isBlank()
        ) {
            _uiState.value = SignupUiState.Error("Todos los campos son obligatorios")
            return
        }

        if (displayName.length < 2) {
            _uiState.value = SignupUiState.Error("El nombre debe tener al menos 2 caracteres")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = SignupUiState.Error("Ingresa un email válido")
            return
        }

        if (password.length < 6) {
            _uiState.value = SignupUiState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        if (password != confirmPassword) {
            _uiState.value = SignupUiState.Error("Las contraseñas no coinciden")
            return
        }

        if (!esMayorDeEdad(birthDate)) {
            _uiState.value = SignupUiState.Error("Debes ser mayor de edad (18 años o más) para registrarte")
            return
        }

        if (!acceptedTerms) {
            _uiState.value = SignupUiState.Error("Debes aceptar los términos y condiciones")
            return
        }

        _uiState.value = SignupUiState.Loading

        viewModelScope.launch {
            try {
                val user = authRepository.signUp(
                    email = email,
                    pass = password,
                    displayName = displayName,
                    birthDate = birthDate
                )
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun esMayorDeEdad(fecha: String): Boolean {
        return try {
            val birth = LocalDate.parse(fecha) // espera formato yyyy-MM-dd
            val today = LocalDate.now()
            val edad = Period.between(birth, today).years
            edad >= 18
        } catch (e: DateTimeParseException) {
            false
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
