package com.example.ovyu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ovyu.repository.AppointmentRepository
import com.example.ovyu.repository.ExamRepository
import com.example.ovyu.repository.MedicationRepository
import com.example.ovyu.room.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModel(
    session: SessionManager,
    appointmentRepository: AppointmentRepository,
    medicationRepository: MedicationRepository,
    examRepository: ExamRepository
) : ViewModel() {

    val uiState: StateFlow<ListState<AppNotification>> = session.patientId
        .filterNotNull()
        .flatMapLatest { patientId ->
            combine(
                appointmentRepository.observeAll(patientId),
                medicationRepository.observeAll(patientId),
                examRepository.observeAll(patientId)
            ) { appointments, medications, exams ->
                ListState(
                    isLoading = false,
                    items = buildNotifications(appointments, medications, exams)
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListState())
}
