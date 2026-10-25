package com.example.ovyu.compose.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovyu.compose.appointment.AppointmentCard
import com.example.ovyu.compose.components.EmptyState
import com.example.ovyu.compose.components.HeroCard
import com.example.ovyu.compose.components.IconBadge
import com.example.ovyu.compose.components.LoadingBox
import com.example.ovyu.compose.components.OvyuCard
import com.example.ovyu.compose.components.PrimaryButton
import com.example.ovyu.compose.components.ScreenHeader
import com.example.ovyu.compose.components.SectionLabel
import com.example.ovyu.ui.theme.OvyuAmber
import com.example.ovyu.ui.theme.OvyuBlue
import com.example.ovyu.ui.theme.OvyuGreen
import com.example.ovyu.ui.theme.OvyuRed
import com.example.ovyu.util.toDisplayDate
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.HealthUiState
import com.example.ovyu.viewmodel.HealthViewModel

/** Cor associada à intensidade do sintoma (1-10). */
fun intensityColor(intensity: Int): Color = when {
    intensity <= 3 -> OvyuGreen
    intensity <= 6 -> OvyuAmber
    else -> OvyuRed
}

@Composable
fun HealthScreen(
    onRegisterSymptom: () -> Unit,
    onSymptomsClick: () -> Unit,
    onMedicationsClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onExamsClick: () -> Unit,
    onAppointmentClick: (Long) -> Unit,
    onSymptomClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HealthContent(
        state = state,
        modifier = modifier,
        onRegisterSymptom = onRegisterSymptom,
        onSymptomsClick = onSymptomsClick,
        onMedicationsClick = onMedicationsClick,
        onAppointmentsClick = onAppointmentsClick,
        onExamsClick = onExamsClick,
        onAppointmentClick = onAppointmentClick,
        onSymptomClick = onSymptomClick
    )
}

@Composable
fun HealthContent(
    state: HealthUiState,
    modifier: Modifier = Modifier,
    onRegisterSymptom: () -> Unit = {},
    onSymptomsClick: () -> Unit = {},
    onMedicationsClick: () -> Unit = {},
    onAppointmentsClick: () -> Unit = {},
    onExamsClick: () -> Unit = {},
    onAppointmentClick: (Long) -> Unit = {},
    onSymptomClick: (Long) -> Unit = {}
) {
    if (state.isLoading) {
        LoadingBox(modifier)
        return
    }

    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        ScreenHeader(
            title = "Minha saúde",
            subtitle = "Um resumo das suas informações"
        )

        Spacer(modifier = Modifier.height(20.dp))

        HeroCard(
            icon = Icons.Default.MonitorHeart,
            title = "Resumo da semana",
            subtitle = if (state.symptomsLast7Days == 0) {
                "Nenhum sintoma registrado nos últimos 7 dias."
            } else {
                "${state.symptomsLast7Days} sintoma(s) registrado(s) nos últimos 7 dias."
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Indicadores
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = Icons.Default.FavoriteBorder,
                value = state.symptomsLast7Days.toString(),
                label = "Sintomas (7 dias)",
                modifier = Modifier.weight(1f),
                onClick = onSymptomsClick
            )
            StatTile(
                icon = Icons.Default.Medication,
                value = state.activeMedications.size.toString(),
                label = "Medicamentos em uso",
                modifier = Modifier.weight(1f),
                onClick = onMedicationsClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = Icons.Default.CalendarMonth,
                value = state.upcomingAppointmentCount.toString(),
                label = "Consultas agendadas",
                modifier = Modifier.weight(1f),
                onClick = onAppointmentsClick
            )
            StatTile(
                icon = Icons.Default.Science,
                value = state.pendingExams.toString(),
                label = "Exames pendentes",
                modifier = Modifier.weight(1f),
                onClick = onExamsClick
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Gráfico de intensidade
        SectionLabel("Intensidade nos últimos 7 dias")
        Spacer(modifier = Modifier.height(12.dp))
        IntensityChart(week = state.week)

        Spacer(modifier = Modifier.height(26.dp))

        // Próxima consulta
        SectionLabel("Próxima consulta")
        Spacer(modifier = Modifier.height(12.dp))
        val next = state.nextAppointment
        if (next != null) {
            AppointmentCard(appointment = next, onClick = { onAppointmentClick(next.id) })
        } else {
            OvyuCard(onClick = onAppointmentsClick) {
                EmptyState(
                    icon = Icons.Default.CalendarMonth,
                    title = "Nenhuma consulta agendada",
                    message = "Cadastre uma consulta para acompanhar sua agenda."
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Medicamentos em uso
        SectionLabel("Medicamentos em uso")
        Spacer(modifier = Modifier.height(12.dp))
        if (state.activeMedications.isEmpty()) {
            Text(
                text = "Nenhum medicamento em uso hoje.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        } else {
            OvyuCard(onClick = onMedicationsClick) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.activeMedications.take(4).forEach { medication ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(icon = Icons.Default.Medication, size = 36.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = medication.name,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "${medication.dosage} • ${medication.frequency}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Sintomas recentes
        SectionLabel("Sintomas recentes")
        Spacer(modifier = Modifier.height(12.dp))
        if (state.recentSymptoms.isEmpty()) {
            Text(
                text = "Você ainda não registrou sintomas.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.recentSymptoms.forEach { symptom ->
                    OvyuCard(onClick = { onSymptomClick(symptom.id) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(intensityColor(symptom.intensity).copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = symptom.intensity.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = intensityColor(symptom.intensity)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = symptom.symptom,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "${symptom.startDate.toDisplayDate()} • ${symptom.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = "Registrar sintoma",
            icon = Icons.Default.FavoriteBorder,
            onClick = onRegisterSymptom
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OvyuCard(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            IconBadge(icon = icon, size = 38.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IntensityChart(week: List<com.example.ovyu.viewmodel.DayIntensity>) {
    val colors = MaterialTheme.colorScheme
    val maxBarHeight = 110.dp

    OvyuCard {
        Column(modifier = Modifier.padding(16.dp)) {
            if (week.none { it.maxIntensity > 0 }) {
                Text(
                    text = "Registre sintomas para ver a evolução da intensidade.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                week.forEach { day ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier.height(maxBarHeight + 18.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (day.maxIntensity > 0) {
                                    Text(
                                        text = day.maxIntensity.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.onSurface
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .height(
                                            if (day.maxIntensity > 0) maxBarHeight * (day.maxIntensity / 10f)
                                            else 4.dp
                                        )
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (day.maxIntensity > 0) intensityColor(day.maxIntensity)
                                            else colors.outlineVariant
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = day.label,
                            fontSize = 11.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
