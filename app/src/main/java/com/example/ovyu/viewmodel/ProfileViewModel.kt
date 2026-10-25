package com.example.ovyu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ovyu.repository.AppointmentRepository
import com.example.ovyu.repository.AuthRepository
import com.example.ovyu.repository.ExamRepository
import com.example.ovyu.repository.MedicationRepository
import com.example.ovyu.repository.SymptomRepository
import com.example.ovyu.room.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val createdAt: Long = 0L,
    val symptomCount: Int = 0,
    val medicationCount: Int = 0,
    val appointmentCount: Int = 0,
    val examCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val session: SessionManager,
    private val authRepository: AuthRepository,
    symptomRepository: SymptomRepository,
    medicationRepository: MedicationRepository,
    appointmentRepository: AppointmentRepository,
    examRepository: ExamRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = session.patientId
        .filterNotNull()
        .flatMapLatest { patientId ->
            combine(
                authRepository.observePatient(patientId),
                symptomRepository.observeAll(patientId),
                medicationRepository.observeAll(patientId),
                appointmentRepository.observeAll(patientId),
                examRepository.observeAll(patientId)
            ) { patient, symptoms, medications, appointments, exams ->
                ProfileUiState(
                    isLoading = false,
                    name = patient?.name.orEmpty(),
                    email = patient?.email.orEmpty(),
                    createdAt = patient?.createdAt ?: 0L,
                    symptomCount = symptoms.size,
                    medicationCount = medications.size,
                    appointmentCount = appointments.size,
                    examCount = exams.size
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun updateName(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val id = session.currentPatientId() ?: return@launch
            authRepository.updateName(id, newName)
        }
    }

    /** Encerra a sessão; o app volta para o Login automaticamente. */
    fun logout() {
        viewModelScope.launch { session.clearSession() }
    }

    /** Apaga a conta e todos os dados locais do paciente. */
    fun deleteAccount() {
        viewModelScope.launch {
            val id = session.currentPatientId() ?: return@launch
            authRepository.deleteAccount(id)
            session.clearSession()
        }
    }
}
