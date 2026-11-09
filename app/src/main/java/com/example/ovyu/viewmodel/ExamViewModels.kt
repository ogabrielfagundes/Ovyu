package com.example.ovyu.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ovyu.repository.ExamRepository
import com.example.ovyu.room.ExamEntity
import com.example.ovyu.room.ExamStatus
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

data class ExamsUiState(
    val isLoading: Boolean = true,
    val pending: List<ExamEntity> = emptyList(),
    val done: List<ExamEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExamsViewModel(
    session: SessionManager,
    repository: ExamRepository
) : ViewModel() {

    val uiState: StateFlow<ExamsUiState> = session.patientId
        .filterNotNull()
        .flatMapLatest { repository.observeAll(it) }
        .map { all ->
            val (done, pending) = all.partition { it.status == ExamStatus.DONE }
            ExamsUiState(
                isLoading = false,
                pending = pending.sortedBy { it.date },
                done = done.sortedByDescending { it.date }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExamsUiState())
}

// ----------------------------------------------------------- Formulário

data class ExamFormState(
    val name: String = "",
    val date: LocalDate = LocalDate.now(),
    val lab: String = "",
    val status: String = ExamStatus.SCHEDULED,
    val result: String = "",
    val nameError: Boolean = false,
    val dateError: Boolean = false,
    val dateErrorText: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val finished: Boolean = false
)

class ExamFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val session: SessionManager,
    private val repository: ExamRepository
) : ViewModel() {

    private val editingId: Long = (savedStateHandle.get<Long>(ID_ARG) ?: -1L).coerceAtLeast(0L)
    private var original: ExamEntity? = null

    private val _state = MutableStateFlow(
        ExamFormState(isEditing = editingId > 0, isLoading = editingId > 0)
    )
    val state: StateFlow<ExamFormState> = _state.asStateFlow()

    init {
        if (editingId > 0) load()
    }

    fun update(transform: (ExamFormState) -> ExamFormState) {
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
                    name = entity.name,
                    date = entity.date.toLocalDateOrNull() ?: LocalDate.now(),
                    lab = entity.lab,
                    status = entity.status,
                    result = entity.result,
                    isLoading = false
                )
            }
        }
    }

    fun save() {
        val s = _state.value
        val nameError = s.name.isBlank()
        val dateErrorText = examDateProblem(s.status, s.date)
        val dateError = dateErrorText != null

        if (nameError || dateError) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    dateError = dateError,
                    dateErrorText = dateErrorText.orEmpty()
                )
            }
            return
        }

        viewModelScope.launch {
            val patientId = session.currentPatientId() ?: return@launch
            repository.save(
                ExamEntity(
                    id = editingId,
                    patientId = patientId,
                    name = s.name.trim(),
                    date = s.date.toString(),
                    lab = s.lab.trim(),
                    status = s.status,
                    result = s.result.trim(),
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

/**
 * Mesma regra das consultas, por dia: "Agendado" não pode ser antes de hoje e
 * "Realizado" não pode ser depois de hoje (não existe resultado de exame futuro).
 */
fun examDateProblem(
    status: String,
    date: LocalDate,
    today: LocalDate = LocalDate.now()
): String? = when {
    status == ExamStatus.SCHEDULED && date.isBefore(today) ->
        "Um exame agendado não pode ter data passada. " +
                "Se ele já foi feito, altere a situação para Realizado."
    status == ExamStatus.DONE && date.isAfter(today) ->
        "Um exame realizado não pode ter data futura. " +
                "Se ele ainda vai acontecer, altere a situação para Agendado."
    else -> null
}
