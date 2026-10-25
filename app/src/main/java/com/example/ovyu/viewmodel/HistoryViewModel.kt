package com.example.ovyu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ovyu.repository.AppointmentRepository
import com.example.ovyu.repository.ExamRepository
import com.example.ovyu.repository.MedicationRepository
import com.example.ovyu.repository.SymptomRepository
import com.example.ovyu.room.SessionManager
import com.example.ovyu.util.format
import com.example.ovyu.util.startOfDayMillis
import com.example.ovyu.util.toLocalDateOrNull
import com.example.ovyu.util.toLocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class HistoryType(val label: String) {
    SYMPTOM("Sintomas"),
    APPOINTMENT("Consultas"),
    EXAM("Exames"),
    MEDICATION("Medicamentos")
}

data class HistoryItem(
    val type: HistoryType,
    val id: Long,
    val title: String,
    val subtitle: String,
    val dateLabel: String,
    val sortKey: Long,
    val badge: String? = null
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val filter: HistoryType? = null,
    val items: List<HistoryItem> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    session: SessionManager,
    symptomRepository: SymptomRepository,
    medicationRepository: MedicationRepository,
    appointmentRepository: AppointmentRepository,
    examRepository: ExamRepository
) : ViewModel() {

    private val filter = MutableStateFlow<HistoryType?>(null)

    private val allItems = session.patientId
        .filterNotNull()
        .flatMapLatest { patientId ->
            combine(
                symptomRepository.observeAll(patientId),
                medicationRepository.observeAll(patientId),
                appointmentRepository.observeAll(patientId),
                examRepository.observeAll(patientId)
            ) { symptoms, medications, appointments, exams ->
                val items = mutableListOf<HistoryItem>()

                symptoms.forEach {
                    val date = it.startDate.toLocalDateOrNull()
                    items += HistoryItem(
                        type = HistoryType.SYMPTOM,
                        id = it.id,
                        title = it.symptom,
                        subtitle = "${it.category} • ${it.duration}",
                        dateLabel = date?.format() ?: it.startDate,
                        sortKey = date?.startOfDayMillis() ?: it.createdAt,
                        badge = "${it.intensity}/10"
                    )
                }
                appointments.forEach {
                    val dt = it.dateTime.toLocalDateTime()
                    items += HistoryItem(
                        type = HistoryType.APPOINTMENT,
                        id = it.id,
                        title = it.doctor,
                        subtitle = "${it.specialty} • ${it.status}",
                        dateLabel = "${dt.toLocalDate().format()} ${dt.toLocalTime().format()}",
                        sortKey = it.dateTime
                    )
                }
                exams.forEach {
                    val date = it.date.toLocalDateOrNull()
                    items += HistoryItem(
                        type = HistoryType.EXAM,
                        id = it.id,
                        title = it.name,
                        subtitle = it.status + if (it.lab.isNotBlank()) " • ${it.lab}" else "",
                        dateLabel = date?.format() ?: it.date,
                        sortKey = date?.startOfDayMillis() ?: it.createdAt
                    )
                }
                medications.forEach {
                    val date = it.startDate.toLocalDateOrNull()
                    items += HistoryItem(
                        type = HistoryType.MEDICATION,
                        id = it.id,
                        title = it.name,
                        subtitle = "${it.dosage} • ${it.frequency}",
                        dateLabel = "Início: " + (date?.format() ?: it.startDate),
                        sortKey = date?.startOfDayMillis() ?: it.createdAt
                    )
                }
                items.sortedByDescending { it.sortKey }
            }
        }

    val uiState: StateFlow<HistoryUiState> = combine(allItems, filter) { items, selected ->
        HistoryUiState(
            isLoading = false,
            filter = selected,
            items = if (selected == null) items else items.filter { it.type == selected }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun setFilter(type: HistoryType?) {
        filter.update { type }
    }
}
