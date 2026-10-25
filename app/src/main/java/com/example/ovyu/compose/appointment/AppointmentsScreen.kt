package com.example.ovyu.compose.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovyu.compose.components.EmptyState
import com.example.ovyu.compose.components.LoadingBox
import com.example.ovyu.compose.components.OvyuCard
import com.example.ovyu.compose.components.ScreenHeader
import com.example.ovyu.compose.components.SectionLabel
import com.example.ovyu.compose.components.StatusChip
import com.example.ovyu.room.AppointmentEntity
import com.example.ovyu.room.AppointmentStatus
import com.example.ovyu.ui.theme.OvyuBlue
import com.example.ovyu.ui.theme.OvyuGreen
import com.example.ovyu.ui.theme.OvyuRed
import com.example.ovyu.util.MonthShortFormatter
import com.example.ovyu.util.format
import com.example.ovyu.util.toLocalDateTime
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.AppointmentsUiState
import com.example.ovyu.viewmodel.AppointmentsViewModel

@Composable
fun AppointmentsScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: AppointmentsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AppointmentsContent(state = state, onBack = onBack, onAdd = onAdd, onEdit = onEdit)
}

@Composable
fun AppointmentsContent(
    state: AppointmentsUiState,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit
) {
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
                        title = "Consultas",
                        subtitle = "Sua agenda de consultas médicas",
                        onBack = onBack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (state.upcoming.isEmpty() && state.past.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.CalendarMonth,
                            title = "Nenhuma consulta cadastrada",
                            message = "Cadastre suas consultas para acompanhar sua agenda.",
                            actionLabel = "Agendar consulta",
                            onAction = onAdd
                        )
                    }
                }

                if (state.upcoming.isNotEmpty()) {
                    item { SectionLabel("Próximas") }
                    items(state.upcoming, key = { "up-${it.id}" }) {
                        AppointmentCard(appointment = it, onClick = { onEdit(it.id) })
                    }
                }

                if (state.past.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionLabel("Anteriores")
                    }
                    items(state.past, key = { "past-${it.id}" }) {
                        AppointmentCard(appointment = it, onClick = { onEdit(it.id) })
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
            text = { Text("Nova consulta") }
        )
    }
}

fun appointmentStatusColor(status: String): Color = when (status) {
    AppointmentStatus.DONE -> OvyuGreen
    AppointmentStatus.CANCELLED -> OvyuRed
    else -> OvyuBlue
}

/** Cartão de consulta (usado na lista e na Home). */
@Composable
fun AppointmentCard(
    appointment: AppointmentEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val dateTime = appointment.dateTime.toLocalDateTime()

    OvyuCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bloco da data
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(OvyuBlue.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dateTime.dayOfMonth.toString().padStart(2, '0'),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OvyuBlue
                )
                Text(
                    text = dateTime.format(MonthShortFormatter).uppercase().removeSuffix("."),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OvyuBlue
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.doctor,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${appointment.specialty} • ${dateTime.toLocalTime().format()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (appointment.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appointment.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            StatusChip(
                text = appointment.status,
                color = appointmentStatusColor(appointment.status)
            )
        }
    }
}
