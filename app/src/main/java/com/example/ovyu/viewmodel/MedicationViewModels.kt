package com.example.ovyu.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ovyu.repository.MedicationRepository
import com.example.ovyu.room.MedicationEntity
import com.example.ovyu.room.SessionManager
import com.example.ovyu.util.toLocalDateOrNull
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

// ---------------------------------------------------------------- Lista

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationsViewModel(
    private val session: SessionManager,
    private val repository: MedicationRepository
) : ViewModel() {

    val uiState: StateFlow<ListState<MedicationEntity>> = session.patientId
        .filterNotNull()
        .flatMapLatest { repository.observeAll(it) }
        .map { ListState(isLoading = false, items = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListState())

    fun setActive(medication: MedicationEntity, active: Boolean) {
        viewModelScope.launch {
            val patientId = session.currentPatientId() ?: return@launch
            repository.setActive(medication.id, patientId, active)
        }
    }
}

// ----------------------------------------------------------- Formulário

data class MedicationFormState(
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val times: List<String> = emptyList(),
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val notes: String = "",
    val active: Boolean = true,
    val nameError: Boolean = false,
    val dosageError: Boolean = false,
    val frequencyError: Boolean = false,
    val endDateError: Boolean = false,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val finished: Boolean = false
)

class MedicationFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val session: SessionManager,
    private val repository: MedicationRepository
) : ViewModel() {

    private val editingId: Long = (savedStateHandle.get<Long>(ID_ARG) ?: -1L).coerceAtLeast(0L)
    private var original: MedicationEntity? = null

    private val _state = MutableStateFlow(
        MedicationFormState(isEditing = editingId > 0, isLoading = editingId > 0)
    )
    val state: StateFlow<MedicationFormState> = _state.asStateFlow()

    init {
        if (editingId > 0) load()
    }

    fun update(transform: (MedicationFormState) -> MedicationFormState) {
        _state.update(transform)
    }

    fun addTime(time: String) {
        _state.update { s ->
            if (time in s.times) s else s.copy(times = (s.times + time).sorted())
        }
    }

    fun removeTime(time: String) {
        _state.update { it.copy(times = it.times - time) }
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
            _state.update {
                it.copy(
                    name = entity.name,
                    dosage = entity.dosage,
                    frequency = entity.frequency,
                    times = entity.times.split(",").map { t -> t.trim() }.filter { t -> t.isNotEmpty() },
                    startDate = entity.startDate.toLocalDateOrNull() ?: LocalDate.now(),
                    endDate = entity.endDate.toLocalDateOrNull(),
                    notes = entity.notes,
                    active = entity.active,
                    isLoading = false
                )
            }
        }
    }

    fun save() {
        val s = _state.value
        val nameError = s.name.isBlank()
        val dosageError = s.dosage.isBlank()
        val frequencyError = s.frequency.isBlank()
        val endDateError = s.endDate != null && s.endDate.isBefore(s.startDate)

        if (nameError || dosageError || frequencyError || endDateError) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    dosageError = dosageError,
                    frequencyError = frequencyError,
                    endDateError = endDateError
                )
            }
            return
        }

        viewModelScope.launch {
            val patientId = session.currentPatientId() ?: return@launch
            repository.save(
                MedicationEntity(
                    id = editingId,
                    patientId = patientId,
                    name = s.name.trim(),
                    dosage = s.dosage.trim(),
                    frequency = s.frequency,
                    times = s.times.joinToString(","),
                    startDate = s.startDate.toString(),
                    endDate = s.endDate?.toString().orEmpty(),
                    notes = s.notes.trim(),
                    active = s.active,
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
