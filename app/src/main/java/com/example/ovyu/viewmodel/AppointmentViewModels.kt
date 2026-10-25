package com.example.ovyu.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ovyu.repository.AppointmentRepository
import com.example.ovyu.room.AppointmentEntity
import com.example.ovyu.room.AppointmentStatus
import com.example.ovyu.room.SessionManager
import com.example.ovyu.util.toEpochMillis
import com.example.ovyu.util.toLocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// ---------------------------------------------------------------- Lista

data class AppointmentsUiState(
    val isLoading: Boolean = true,
    val upcoming: List<AppointmentEntity> = emptyList(),
    val past: List<AppointmentEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentsViewModel(
    session: SessionManager,
    repository: AppointmentRepository
) : ViewModel() {

    val uiState: StateFlow<AppointmentsUiState> = session.patientId
        .filterNotNull()
        .flatMapLatest { repository.observeAll(it) }
        .map { all ->
            val now = System.currentTimeMillis()
            val (upcoming, past) = all.partition {
                it.status == AppointmentStatus.SCHEDULED && it.dateTime >= now
            }
            AppointmentsUiState(
                isLoading = false,
                upcoming = upcoming.sortedBy { it.dateTime },
                past = past.sortedByDescending { it.dateTime }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppointmentsUiState())
}

// ----------------------------------------------------------- Formulário

data class AppointmentFormState(
    val doctor: String = "",
    val specialty: String = "",
    val date: LocalDate = LocalDate.now().plusDays(1),
    val time: LocalTime = LocalTime.of(9, 0),
    val location: String = "",
    val notes: String = "",
    val status: String = AppointmentStatus.SCHEDULED,
    val doctorError: Boolean = false,
    val specialtyError: Boolean = false,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val finished: Boolean = false
)

class AppointmentFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val session: SessionManager,
    private val repository: AppointmentRepository
) : ViewModel() {

    private val editingId: Long = (savedStateHandle.get<Long>(ID_ARG) ?: -1L).coerceAtLeast(0L)
    private var original: AppointmentEntity? = null

    private val _state = MutableStateFlow(
        AppointmentFormState(isEditing = editingId > 0, isLoading = editingId > 0)
    )
    val state: StateFlow<AppointmentFormState> = _state.asStateFlow()

    init {
        if (editingId > 0) load()
    }

    fun update(transform: (AppointmentFormState) -> AppointmentFormState) {
        _state.update(transform)
    }

    private fun load() {
        viewModelScope.launch {
            val patientId = session.currentPatientId()
            val entity = patientId?.let { repository.getById(editingId, it) }
            if (entity == null) {
                _state.update { it.copy(isLoading = false, finished = true) }
                return@launch
            }
            original = entity
            val dateTime = entity.dateTime.toLocalDateTime()
            _state.update {
                it.copy(
                    doctor = entity.doctor,
                    specialty = entity.specialty,
                    date = dateTime.toLocalDate(),
                    time = dateTime.toLocalTime(),
                    location = entity.location,
                    notes = entity.notes,
                    status = entity.status,
                    isLoading = false
                )
            }
        }
    }

    fun save() {
        val s = _state.value
        val doctorError = s.doctor.isBlank()
        val specialtyError = s.specialty.isBlank()

        if (doctorError || specialtyError) {
            _state.update { it.copy(doctorError = doctorError, specialtyError = specialtyError) }
            return
        }

        viewModelScope.launch {
            val patientId = session.currentPatientId() ?: return@launch
            repository.save(
                AppointmentEntity(
                    id = editingId,
                    patientId = patientId,
                    doctor = s.doctor.trim(),
                    specialty = s.specialty.trim(),
                    dateTime = LocalDateTime.of(s.date, s.time).toEpochMillis(),
                    location = s.location.trim(),
                    notes = s.notes.trim(),
                    status = s.status,
                    createdAt = original?.createdAt ?: System.currentTimeMillis()
                )
            )
            _state.update { it.copy(finished = true) }
        }
    }

    fun delete() {
        if (editingId <= 0) return
        viewModelScope.launch {
            val patientId = session.currentPatientId() ?: return@launch
            repository.delete(editingId, patientId)
            _state.update { it.copy(finished = true) }
        }
    }
}
