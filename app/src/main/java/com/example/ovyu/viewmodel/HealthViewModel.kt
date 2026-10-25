package com.example.ovyu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ovyu.repository.AppointmentRepository
import com.example.ovyu.repository.ExamRepository
import com.example.ovyu.repository.MedicationRepository
import com.example.ovyu.repository.SymptomRepository
import com.example.ovyu.room.AppointmentEntity
import com.example.ovyu.room.AppointmentStatus
import com.example.ovyu.room.ExamStatus
import com.example.ovyu.room.MedicationEntity
import com.example.ovyu.room.SessionManager
import com.example.ovyu.room.SymptomEntity
import com.example.ovyu.util.PtBr
import com.example.ovyu.util.toLocalDateOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.TextStyle

/** Maior intensidade de sintoma registrada em um dia (0 = nenhum registro). */
data class DayIntensity(val label: String, val maxIntensity: Int)

data class HealthUiState(
    val isLoading: Boolean = true,
    val symptomsLast7Days: Int = 0,
    val activeMedications: List<MedicationEntity> = emptyList(),
    val nextAppointment: AppointmentEntity? = null,
    val upcomingAppointmentCount: Int = 0,
    val pendingExams: Int = 0,
    val recentSymptoms: List<SymptomEntity> = emptyList(),
    val week: List<DayIntensity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HealthViewModel(
    session: SessionManager,
    symptomRepository: SymptomRepository,
    medicationRepository: MedicationRepository,
    appointmentRepository: AppointmentRepository,
    examRepository: ExamRepository
) : ViewModel() {

    val uiState: StateFlow<HealthUiState> = session.patientId
        .filterNotNull()
        .flatMapLatest { patientId ->
            combine(
                symptomRepository.observeAll(patientId),
                medicationRepository.observeAll(patientId),
                appointmentRepository.observeAll(patientId),
                examRepository.observeAll(patientId)
            ) { symptoms, medications, appointments, exams ->
                val today = LocalDate.now()
                val nowMillis = System.currentTimeMillis()

                val upcoming = appointments.filter {
                    it.status == AppointmentStatus.SCHEDULED && it.dateTime >= nowMillis
                }

                val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
                val week = days.map { day ->
                    val max = symptoms
                        .filter { it.startDate.toLocalDateOrNull() == day }
                        .maxOfOrNull { it.intensity } ?: 0
                    DayIntensity(
                        label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, PtBr)
                            .take(3)
                            .replaceFirstChar { it.uppercase() },
                        maxIntensity = max
                    )
                }

                HealthUiState(
                    isLoading = false,
                    symptomsLast7Days = symptoms.count {
                        val date = it.startDate.toLocalDateOrNull()
                        date != null && !date.isBefore(days.first()) && !date.isAfter(today)
                    },
                    activeMedications = medications.filter { it.isTakenOn(today) },
                    nextAppointment = upcoming.minByOrNull { it.dateTime },
                    upcomingAppointmentCount = upcoming.size,
                    pendingExams = exams.count { it.status == ExamStatus.SCHEDULED },
                    recentSymptoms = symptoms.take(3),
                    week = week
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HealthUiState())
}
