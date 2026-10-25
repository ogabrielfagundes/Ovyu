package com.example.ovyu.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ovyu.repository.SymptomRepository
import com.example.ovyu.room.SessionManager
import com.example.ovyu.room.SymptomEntity
import com.example.ovyu.util.toLocalDateOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SymptomFormState(
    val symptom: String = "",
    val category: String = "",
    val intensity: Float = 5f,
    val startDate: LocalDate = LocalDate.now(),
    val duration: String = "",
    val bodyLocation: String = "",
    val description: String = "",
    val symptomError: Boolean = false,
    val categoryError: Boolean = false,
    val durationError: Boolean = false,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val finished: Boolean = false
)

class SymptomFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val session: SessionManager,
    private val repository: SymptomRepository
) : ViewModel() {

    private val editingId: Long = (savedStateHandle.get<Long>(ID_ARG) ?: -1L).coerceAtLeast(0L)
    private var original: SymptomEntity? = null

    private val _state = MutableStateFlow(
        SymptomFormState(isEditing = editingId > 0, isLoading = editingId > 0)
    )
    val state: StateFlow<SymptomFormState> = _state.asStateFlow()

    init {
        if (editingId > 0) load()
    }

    fun update(transform: (SymptomFormState) -> SymptomFormState) {
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
            _state.update {
                it.copy(
                    symptom = entity.symptom,
                    category = entity.category,
                    intensity = entity.intensity.toFloat(),
                    startDate = entity.startDate.toLocalDateOrNull() ?: LocalDate.now(),
                    duration = entity.duration,
                    bodyLocation = entity.bodyLocation,
                    description = entity.description,
                    isLoading = false
                )
            }
        }
    }

    fun save() {
        val s = _state.value
        val symptomError = s.symptom.isBlank()
        val categoryError = s.category.isBlank()
        val durationError = s.duration.isBlank()

        if (symptomError || categoryError || durationError) {
            _state.update {
                it.copy(
                    symptomError = symptomError,
                    categoryError = categoryError,
                    durationError = durationError
                )
            }
            return
        }

        viewModelScope.launch {
            // O ID do paciente vem da sessão guardada no DataStore.
            val patientId = session.currentPatientId() ?: return@launch
            val now = System.currentTimeMillis()
            repository.save(
                SymptomEntity(
                    id = editingId,
                    patientId = patientId,
                    symptom = s.symptom.trim(),
                    category = s.category,
                    intensity = s.intensity.toInt(),
                    startDate = s.startDate.toString(),
                    duration = s.duration,
                    bodyLocation = s.bodyLocation.trim(),
                    description = s.description.trim(),
                    createdAt = original?.createdAt ?: now,
                    updatedAt = now
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
