package com.example.ovyu.compose.medication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovyu.compose.components.EmptyState
import com.example.ovyu.compose.components.IconBadge
import com.example.ovyu.compose.components.LoadingBox
import com.example.ovyu.compose.components.OvyuCard
import com.example.ovyu.compose.components.ScreenHeader
import com.example.ovyu.compose.components.SectionLabel
import com.example.ovyu.compose.components.StatusChip
import com.example.ovyu.room.MedicationEntity
import com.example.ovyu.room.timesList
import com.example.ovyu.ui.theme.OvyuBlue
import com.example.ovyu.ui.theme.OvyuGreen
import com.example.ovyu.util.toDisplayDate
import com.example.ovyu.util.toLocalDateOrNull
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.ListState
import com.example.ovyu.viewmodel.MedicationsViewModel
import com.example.ovyu.viewmodel.isTakenOn
import java.time.LocalDate

@Composable
fun MedicationsScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: MedicationsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MedicationsContent(
        state = state,
        onBack = onBack,
        onAdd = onAdd,
        onEdit = onEdit,
        onToggleActive = viewModel::setActive
    )
}

@Composable
fun MedicationsContent(
    state: ListState<MedicationEntity>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onToggleActive: (MedicationEntity, Boolean) -> Unit
) {
    val today = LocalDate.now()
    val inUse = state.items.filter { it.isTakenOn(today) }
    val others = state.items.filterNot { it.isTakenOn(today) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.isLoading) {
            LoadingBox()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 22.dp, end = 22.dp, top = 16.dp, bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ScreenHeader(
                        title = "Medicamentos",
                        subtitle = "Acompanhe o que você está tomando",
                        onBack = onBack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (state.items.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Medication,
                            title = "Nenhum medicamento cadastrado",
                            message = "Cadastre seus medicamentos, doses e horários.",
                            actionLabel = "Adicionar medicamento",
                            onAction = onAdd
                        )
                    }
                }

                if (inUse.isNotEmpty()) {
                    item { SectionLabel("Em uso hoje") }
                    items(inUse, key = { "use-${it.id}" }) {
                        MedicationCard(it, onClick = { onEdit(it.id) }, onToggleActive = onToggleActive)
                    }
                }

                if (others.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionLabel("Pausados ou encerrados")
                    }
                    items(others, key = { "other-${it.id}" }) {
                        MedicationCard(it, onClick = { onEdit(it.id) }, onToggleActive = onToggleActive)
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = OvyuBlue,
            contentColor = Color.White,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Novo medicamento") }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedicationCard(
    medication: MedicationEntity,
    onClick: () -> Unit,
    onToggleActive: (MedicationEntity, Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val today = LocalDate.now()
    val end = medication.endDate.toLocalDateOrNull()
    val expired = end != null && end.isBefore(today)

    val statusText = when {
        expired -> "Encerrado"
        !medication.active -> "Pausado"
        else -> "Em uso"
    }
    val statusColor = if (statusText == "Em uso") OvyuGreen else colors.onSurfaceVariant

    OvyuCard(onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Default.Medication)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = colors.onSurface
                    )
                    Text(
                        text = "${medication.dosage} • ${medication.frequency}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                // Pausar / retomar o medicamento
                Switch(
                    checked = medication.active,
                    onCheckedChange = { onToggleActive(medication, it) },
                    enabled = !expired
                )
            }

            val times = medication.timesList()
            if (times.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    times.forEach { time ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(OvyuBlue.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = OvyuBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = time, color = OvyuBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Desde ${medication.startDate.toDisplayDate()} " +
                            if (end != null) "até ${medication.endDate.toDisplayDate()}" else "• uso contínuo",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(text = statusText, color = statusColor)
            }

            if (medication.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = medication.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}
