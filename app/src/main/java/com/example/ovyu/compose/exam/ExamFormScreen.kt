package com.example.ovyu.compose.exam

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovyu.compose.components.DateField
import com.example.ovyu.compose.components.DropdownField
import com.example.ovyu.compose.components.FormActions
import com.example.ovyu.compose.components.FormScreenLayout
import com.example.ovyu.compose.components.FormSectionTitle
import com.example.ovyu.room.ExamStatus
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.ExamFormState
import com.example.ovyu.viewmodel.ExamFormViewModel
import java.time.LocalDate

@Composable
fun ExamFormScreen(
    onBack: () -> Unit,
    viewModel: ExamFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onBack()
    }

    ExamFormContent(
        state = state,
        onChange = viewModel::update,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        onBack = onBack
    )
}

@Composable
fun ExamFormContent(
    state: ExamFormState,
    onChange: ((ExamFormState) -> ExamFormState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    FormScreenLayout(
        title = if (state.isEditing) "Editar exame" else "Novo exame",
        subtitle = "Registre o agendamento ou o resultado.",
        onBack = onBack,
        isLoading = state.isLoading
    ) {
        FormSectionTitle(number = "01", title = "Exame")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { v -> onChange { it.copy(name = v, nameError = false) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nome do exame *") },
            placeholder = { Text("Ex.: Hemograma completo") },
            leadingIcon = { Icon(Icons.Default.Science, contentDescription = null) },
            isError = state.nameError,
            supportingText = { if (state.nameError) Text("Informe o nome do exame.") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        DateField(
            label = "Data *",
            date = state.date,
            onDateSelected = { d -> d?.let { picked -> onChange { it.copy(date = picked) } } },
            yearRange = (LocalDate.now().year - 10)..(LocalDate.now().year + 5)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.lab,
            onValueChange = { v -> onChange { it.copy(lab = v) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Laboratório / local (opcional)") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(22.dp))

        FormSectionTitle(number = "02", title = "Situação e resultado")
        Spacer(modifier = Modifier.height(16.dp))

        DropdownField(
            label = "Situação",
            value = state.status,
            options = ExamStatus.all,
            onSelected = { s -> onChange { it.copy(status = s) } }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.result,
            onValueChange = { v -> onChange { it.copy(result = v) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            label = { Text("Resultado / observações (opcional)") },
            placeholder = { Text("Ex.: valores dentro da normalidade") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            maxLines = 6
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "* Campos obrigatórios",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        FormActions(
            isEditing = state.isEditing,
            saveLabel = if (state.isEditing) "Salvar alterações" else "Salvar exame",
            deleteLabel = "Excluir exame",
            deleteMessage = "Este exame será removido da sua lista. Deseja continuar?",
            onSave = onSave,
            onCancel = onBack,
            onDelete = onDelete
        )
    }
}
