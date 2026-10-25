package com.example.ovyu.compose.medication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovyu.compose.components.DateField
import com.example.ovyu.compose.components.DropdownField
import com.example.ovyu.compose.components.FormActions
import com.example.ovyu.compose.components.FormScreenLayout
import com.example.ovyu.compose.components.FormSectionTitle
import com.example.ovyu.compose.components.TimeListEditor
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.MedicationFormState
import com.example.ovyu.viewmodel.MedicationFormViewModel
import com.example.ovyu.viewmodel.medicationFrequencies
import java.time.LocalDate

@Composable
fun MedicationFormScreen(
    onBack: () -> Unit,
    viewModel: MedicationFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onBack()
    }

    MedicationFormContent(
        state = state,
        onChange = viewModel::update,
        onAddTime = viewModel::addTime,
        onRemoveTime = viewModel::removeTime,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        onBack = onBack
    )
}

@Composable
fun MedicationFormContent(
    state: MedicationFormState,
    onChange: ((MedicationFormState) -> MedicationFormState) -> Unit,
    onAddTime: (String) -> Unit,
    onRemoveTime: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    FormScreenLayout(
        title = if (state.isEditing) "Editar medicamento" else "Novo medicamento",
        subtitle = "Informe a dose e os horários de uso.",
        onBack = onBack,
        isLoading = state.isLoading
    ) {
        FormSectionTitle(number = "01", title = "Medicamento")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { v -> onChange { it.copy(name = v, nameError = false) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nome *") },
            placeholder = { Text("Ex.: Dipirona") },
            leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null) },
            isError = state.nameError,
            supportingText = { if (state.nameError) Text("Informe o nome do medicamento.") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = state.dosage,
            onValueChange = { v -> onChange { it.copy(dosage = v, dosageError = false) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Dosagem *") },
            placeholder = { Text("Ex.: 500 mg, 1 comprimido") },
            isError = state.dosageError,
            supportingText = { if (state.dosageError) Text("Informe a dosagem.") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        DropdownField(
            label = "Frequência *",
            value = state.frequency,
            options = medicationFrequencies,
            onSelected = { f -> onChange { it.copy(frequency = f, frequencyError = false) } },
            placeholder = "Selecione a frequência",
            leadingIcon = Icons.Default.Repeat,
            isError = state.frequencyError,
            errorText = "Selecione a frequência."
        )

        Spacer(modifier = Modifier.height(22.dp))

        FormSectionTitle(number = "02", title = "Horários")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toque em um horário para removê-lo.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        TimeListEditor(
            times = state.times,
            onAdd = onAddTime,
            onRemove = onRemoveTime
        )

        Spacer(modifier = Modifier.height(26.dp))

        FormSectionTitle(number = "03", title = "Período de uso")
        Spacer(modifier = Modifier.height(16.dp))

        DateField(
            label = "Início *",
            date = state.startDate,
            onDateSelected = { d ->
                d?.let { picked -> onChange { it.copy(startDate = picked, endDateError = false) } }
            },
            yearRange = (LocalDate.now().year - 10)..(LocalDate.now().year + 5)
        )

        Spacer(modifier = Modifier.height(12.dp))

        DateField(
            label = "Fim (opcional)",
            date = state.endDate,
            onDateSelected = { d -> onChange { it.copy(endDate = d, endDateError = false) } },
            yearRange = (LocalDate.now().year - 10)..(LocalDate.now().year + 5),
            placeholder = "Uso contínuo",
            clearable = true,
            isError = state.endDateError,
            errorText = "O fim deve ser depois do início."
        )

        Spacer(modifier = Modifier.height(22.dp))

        FormSectionTitle(number = "04", title = "Observações")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.notes,
            onValueChange = { v -> onChange { it.copy(notes = v) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            label = { Text("Observações (opcional)") },
            placeholder = { Text("Ex.: tomar após as refeições") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Em uso",
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    text = "Desative para pausar sem excluir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            Switch(
                checked = state.active,
                onCheckedChange = { checked -> onChange { it.copy(active = checked) } }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "* Campos obrigatórios",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        FormActions(
            isEditing = state.isEditing,
            saveLabel = if (state.isEditing) "Salvar alterações" else "Salvar medicamento",
            deleteLabel = "Excluir medicamento",
            deleteMessage = "Este medicamento será removido da sua lista. Deseja continuar?",
            onSave = onSave,
            onCancel = onBack,
            onDelete = onDelete
        )
    }
}
