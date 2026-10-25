package com.example.ovyu.compose.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovyu.compose.appointment.AppointmentCard
import com.example.ovyu.compose.components.OvyuGradient
import com.example.ovyu.compose.components.StatusChip
import com.example.ovyu.ui.theme.OvyuBlue
import com.example.ovyu.ui.theme.OvyuPurple
import com.example.ovyu.ui.theme.OvyuTheme
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.HomeUiState
import com.example.ovyu.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    onHealthOverviewClick: () -> Unit = {},
    onSymptomsClick: () -> Unit = {},
    onMedicationsClick: () -> Unit = {},
    onAppointmentsClick: () -> Unit = {},
    onExamsClick: () -> Unit = {},
    onAppointmentClick: (Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        onHealthOverviewClick = onHealthOverviewClick,
        onSymptomsClick = onSymptomsClick,
        onMedicationsClick = onMedicationsClick,
        onAppointmentsClick = onAppointmentsClick,
        onExamsClick = onExamsClick,
        onAppointmentClick = onAppointmentClick
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    onHealthOverviewClick: () -> Unit = {},
    onSymptomsClick: () -> Unit = {},
    onMedicationsClick: () -> Unit = {},
    onAppointmentsClick: () -> Unit = {},
    onExamsClick: () -> Unit = {},
    onAppointmentClick: (Long) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val greetingName = state.userName.ifBlank { "Usuário" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Cabeçalho
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Olá, $greetingName!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Como você está se sentindo hoje?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.65f))
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Notificações",
                        tint = colorScheme.onSurface
                    )
                }

                // Bolinha indicando que há avisos
                if (state.notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, end = 10.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colorScheme.error)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Cartão principal de saúde
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onHealthOverviewClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = OvyuBlue)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OvyuGradient)
                    .padding(22.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Sua saúde em um só lugar",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Acompanhe suas informações de saúde " +
                                "e mantenha seu histórico organizado.",
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ver minha saúde",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Acesso rápido
        Text(
            text = "Acesso rápido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Sintomas",
                subtitle = "Registrar",
                icon = Icons.Default.FavoriteBorder,
                modifier = Modifier.weight(1f),
                onClick = onSymptomsClick
            )

            QuickActionCard(
                title = "Medicamentos",
                subtitle = "Acompanhar",
                icon = Icons.Default.Medication,
                modifier = Modifier.weight(1f),
                onClick = onMedicationsClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Consultas",
                subtitle = "Agendamentos",
                icon = Icons.Default.CalendarMonth,
                modifier = Modifier.weight(1f),
                onClick = onAppointmentsClick
            )

            QuickActionCard(
                title = "Exames",
                subtitle = "Resultados",
                icon = Icons.Default.Science,
                modifier = Modifier.weight(1f),
                onClick = onExamsClick
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Próximas consultas
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Próximas consultas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )

            Text(
                text = "Ver todas",
                color = OvyuBlue,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable(onClick = onAppointmentsClick)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (state.upcomingAppointments.isEmpty()) {
            EmptyAppointmentsCard(
                isLoading = state.isLoading,
                onAppointmentsClick = onAppointmentsClick
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.upcomingAppointments.forEach { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onClick = { onAppointmentClick(appointment.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Área reservada para futuras funcionalidades de IA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            border = BorderStroke(1.dp, colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(OvyuBlue, OvyuPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ovyu Inteligência",
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusChip(text = "Em breve", color = OvyuPurple)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Uma experiência inteligente para " +
                                "organizar suas informações de saúde.",
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun EmptyAppointmentsCard(
    isLoading: Boolean,
    onAppointmentsClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(OvyuBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = OvyuBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isLoading) "Carregando..." else "Nenhuma consulta agendada",
                color = colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Suas próximas consultas aparecerão aqui.",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Acessar consultas",
                color = OvyuBlue,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAppointmentsClick)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier
            .height(142.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(OvyuBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OvyuBlue
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    OvyuTheme {
        HomeContent(state = HomeUiState(isLoading = false, userName = "Gabriel"))
    }
}
