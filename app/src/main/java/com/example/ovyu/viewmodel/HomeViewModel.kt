package com.example.ovyu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ovyu.repository.AppointmentRepository
import com.example.ovyu.repository.AuthRepository
import com.example.ovyu.repository.ExamRepository
import com.example.ovyu.repository.MedicationRepository
import com.example.ovyu.room.AppointmentEntity
import com.example.ovyu.room.AppointmentStatus
import com.example.ovyu.room.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val upcomingAppointments: List<AppointmentEntity> = emptyList(),
    val notificationCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    session: SessionManager,
    authRepository: AuthRepository,
    appointmentRepository: AppointmentRepository,
    medicationRepository: MedicationRepository,
    examRepository: ExamRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = session.patientId
        .filterNotNull()
        .flatMapLatest { patientId ->
            combine(
                authRepository.observePatient(patientId),
                appointmentRepository.observeAll(patientId),
                medicationRepository.observeAll(patientId),
                examRepository.observeAll(patientId)
            ) { patient, appointments, medications, exams ->
                val nowMillis = System.currentTimeMillis()
                HomeUiState(
                    isLoading = false,
                    userName = patient?.name?.trim()?.substringBefore(' ').orEmpty(),
                    upcomingAppointments = appointments
                        .filter {
                            it.status == AppointmentStatus.SCHEDULED && it.dateTime >= nowMillis
                        }
                        .sortedBy { it.dateTime }
                        .take(3),
                    notificationCount = buildNotifications(appointments, medications, exams).size
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
