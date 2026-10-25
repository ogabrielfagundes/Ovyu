package com.example.ovyu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ovyu.repository.AuthRepository
import com.example.ovyu.repository.AuthResult
import com.example.ovyu.room.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Login e cadastro. Em caso de sucesso grava o ID do paciente no DataStore;
 * o SessionViewModel percebe a mudança e o app navega para a Home sozinho.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            handle(authRepository.login(email, password))
        }
    }

    fun register(name: String, email: String, password: String) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            handle(authRepository.register(name, email, password))
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun handle(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                session.saveSession(result.patient.id)
                _uiState.value = AuthUiState()
            }

            is AuthResult.Error -> _uiState.value = AuthUiState(error = result.message)
        }
    }
}
