package com.example.ovyu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ovyu.repository.AuthRepository
import com.example.ovyu.room.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val patientId: Long) : SessionState
}

/**
 * Decide se o app abre no Login ou na Home, lendo a sessão salva no DataStore.
 * Se o ID salvo não existir mais no Room (ex.: conta apagada), volta para o Login.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModel(
    private val session: SessionManager,
    authRepository: AuthRepository
) : ViewModel() {

    val state: StateFlow<SessionState> = session.patientId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(SessionState.LoggedOut)
            } else {
                authRepository.observePatient(id).map { patient ->
                    if (patient == null) {
                        session.clearSession()
                        SessionState.LoggedOut
                    } else {
                        SessionState.LoggedIn(id)
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)
}
