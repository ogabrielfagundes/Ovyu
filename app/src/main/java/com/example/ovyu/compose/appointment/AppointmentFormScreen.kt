package com.example.ovyu.compose.appointment

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
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
import com.example.ovyu.compose.components.TimeField
import com.example.ovyu.room.AppointmentStatus
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.AppointmentFormState
import com.example.ovyu.viewmodel.AppointmentFormViewModel
import java.time.LocalDate

@Composable
fun AppointmentFormScreen(
    onBack: () -> Unit,
    viewModel: AppointmentFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onBack()
    }

    AppointmentFormContent(
        state = state,
        onChange = viewModel::update,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        onBack = onBack
    )
}

@Composable
fun AppointmentFormContent(
    state: AppointmentFormState,
    onChange: ((AppointmentFormState) -> AppointmentFormState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    FormScreenLayout(
        title = if (state.isEditing) "Editar consulta" else "Nova consulta",
        subtitle = "Informe os dados do atendimento.",
        onBack = onBack,
        isLoading = state.isLoading
    ) {
        FormSectionTitle(number = "01", title = "Profissional")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.doctor,
            onValueChange = { v -> onChange { it.copy(doctor = v, doctorError = false) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Médico(a) *") },
            placeholder = { Text("Ex.: Dra. Ana Souza") },
            leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Person, null) },
            isError = state.doctorError,
            supportingText = { if (state.doctorError) Text("Informe o nome do médico.") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = state.specialty,
            onValueChange = { v -> onChange { it.copy(specialty = v, specialtyError = false) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Especialidade *") },
            placeholder = { Text("Ex.: Cardiologia") },
            leadingIcon = { androidx.compose.material3.Icon(Icons.Default.MedicalServices, null) },
            isError = state.specialtyError,
            supportingText = { if (state.specialtyError) Text("Informe a especialidade.") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(22.dp))

        FormSectionTitle(number = "02", title = "Data e horário")
        Spacer(modifier = Modifier.height(16.dp))

        DateField(
            label = "Data *",
            date = state.date,
            onDateSelected = { d -> d?.let { picked -> onChange { it.copy(date = picked) } } },
            yearRange = (LocalDate.now().year - 5)..(LocalDate.now().year + 5)
        )

        Spacer(modifier = Modifier.height(12.dp))

        TimeField(
            label = "Horário *",
            time = state.time,
            onTimeSelected = { t -> onChange { it.copy(time = t) } }
        )

        Spacer(modifier = Modifier.height(22.dp))

        FormSectionTitle(number = "03", title = "Detalhes")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.location,
            onValueChange = { v -> onChange { it.copy(location = v) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Local (opcional)") },
            placeholder = { Text("Ex.: Clínica Saúde, sala 12") },
            leadingIcon = { androidx.compose.material3.Icon(Icons.Default.LocationOn, null) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.notes,
            onValueChange = { v -> onChange { it.copy(notes = v) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            label = { Text("Observações (opcional)") },
            placeholder = { Text("Ex.: levar exames anteriores") },
            leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Description, null) },
            shape = RoundedCornerShape(16.dp),
            maxLines = 4
        )

        if (state.isEditing) {
            Spacer(modifier = Modifier.height(12.dp))
            DropdownField(
                label = "Situação",
                value = state.status,
                options = AppointmentStatus.all,
                onSelected = { s -> onChange { it.copy(status = s) } }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "* Campos obrigatórios",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        FormActions(
            isEditing = state.isEditing,
            saveLabel = if (state.isEditing) "Salvar alterações" else "Salvar consulta",
            deleteLabel = "Excluir consulta",
            deleteMessage = "Esta consulta será removida da sua agenda. Deseja continuar?",
            onSave = onSave,
            onCancel = onBack,
            onDelete = onDelete
        )
    }
}
