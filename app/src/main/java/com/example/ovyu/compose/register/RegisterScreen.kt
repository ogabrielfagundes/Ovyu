package com.example.ovyu.compose.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovyu.compose.components.DateField
import com.example.ovyu.compose.components.DropdownField
import com.example.ovyu.compose.components.FormActions
import com.example.ovyu.compose.components.FormScreenLayout
import com.example.ovyu.compose.components.FormSectionTitle
import com.example.ovyu.compose.components.HeroCard
import com.example.ovyu.ui.theme.OvyuBlue
import com.example.ovyu.ui.theme.OvyuTheme
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.SymptomFormState
import com.example.ovyu.viewmodel.SymptomFormViewModel
import com.example.ovyu.viewmodel.symptomCategories
import com.example.ovyu.viewmodel.symptomDurations
import java.time.LocalDate

/**
 * Formulário de sintomas (novo registro ou edição).
 * Ao salvar, o ViewModel lê o ID do paciente logado no DataStore e grava no Room.
 */
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    viewModel: SymptomFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onBack()
    }

    RegisterContent(
        state = state,
        onChange = viewModel::update,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        onBack = onBack
    )
}

@Composable
fun RegisterContent(
    state: SymptomFormState,
    onChange: ((SymptomFormState) -> SymptomFormState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    FormScreenLayout(
        title = if (state.isEditing) "Editar sintoma" else "Registro de sintomas",
        subtitle = "Conte como você está se sentindo. " +
                "Essas informações ajudarão a manter seu histórico de saúde organizado.",
        onBack = onBack,
        isLoading = state.isLoading
    ) {
        // Cartão de identificação do formulário
        HeroCard(
            icon = Icons.Default.FavoriteBorder,
            title = "Como você está?",
            subtitle = "Preencha os campos abaixo com suas informações."
        )

        Spacer(modifier = Modifier.height(26.dp))

        FormSectionTitle(number = "01", title = "Informações do sintoma")

        Spacer(modifier = Modifier.height(16.dp))

        // Sintoma principal
        OutlinedTextField(
            value = state.symptom,
            onValueChange = { v -> onChange { it.copy(symptom = v, symptomError = false) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Sintoma principal *") },
            placeholder = { Text("Ex.: dor de cabeça") },
            leadingIcon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null) },
            isError = state.symptomError,
            supportingText = {
                if (state.symptomError) {
                    Text("Informe o sintoma principal.")
                } else {
                    Text("Descreva o que você está sentindo.")
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Categoria
        DropdownField(
            label = "Categoria *",
            value = state.category,
            options = symptomCategories,
            onSelected = { c -> onChange { it.copy(category = c, categoryError = false) } },
            placeholder = "Selecione uma categoria",
            isError = state.categoryError,
            errorText = "Selecione uma categoria."
        )

        Spacer(modifier = Modifier.height(22.dp))

        FormSectionTitle(number = "02", title = "Intensidade do sintoma")

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Qual é a intensidade?",
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "De 1 (leve) a 10 (muito intenso)",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Text(
                        text = state.intensity.toInt().toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = OvyuBlue
                    )
                }

                Slider(
                    value = state.intensity,
                    onValueChange = { v -> onChange { it.copy(intensity = v) } },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Leve", "Moderado", "Intenso").forEach { label ->
                        Text(
                            text = label,
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        FormSectionTitle(number = "03", title = "Início e localização")

        Spacer(modifier = Modifier.height(16.dp))

        // Data de início
        DateField(
            label = "Data de início *",
            date = state.startDate,
            onDateSelected = { d -> d?.let { picked -> onChange { it.copy(startDate = picked) } } },
            yearRange = 1900..LocalDate.now().year
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Duração
        DropdownField(
            label = "Há quanto tempo? *",
            value = state.duration,
            options = symptomDurations,
            onSelected = { d -> onChange { it.copy(duration = d, durationError = false) } },
            placeholder = "Selecione a duração",
            isError = state.durationError,
            errorText = "Selecione há quanto tempo começou."
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Local do corpo
        OutlinedTextField(
            value = state.bodyLocation,
            onValueChange = { v -> onChange { it.copy(bodyLocation = v) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Local do corpo (opcional)") },
            placeholder = { Text("Ex.: cabeça, costas, abdômen") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(26.dp))

        FormSectionTitle(number = "04", title = "Detalhes adicionais")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.description,
            onValueChange = { v -> onChange { it.copy(description = v) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            label = { Text("Descrição (opcional)") },
            placeholder = {
                Text(
                    "Descreva outros detalhes, como quando " +
                            "o sintoma aparece ou o que parece piorá-lo..."
                )
            },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "* Campos obrigatórios",
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        FormActions(
            isEditing = state.isEditing,
            saveLabel = if (state.isEditing) "Salvar alterações" else "Salvar registro",
            deleteLabel = "Excluir registro",
            deleteMessage = "Este registro de sintoma será removido do seu histórico. Deseja continuar?",
            onSave = onSave,
            onCancel = onBack,
            onDelete = onDelete
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Este registro serve para acompanhar suas informações. " +
                    "Ele não substitui uma avaliação médica.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    OvyuTheme {
        RegisterContent(
            state = SymptomFormState(),
            onChange = {},
            onSave = {},
            onDelete = {},
            onBack = {}
        )
    }
}
