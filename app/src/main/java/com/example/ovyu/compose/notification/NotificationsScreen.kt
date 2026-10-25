package com.example.ovyu.compose.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovyu.compose.components.EmptyState
import com.example.ovyu.compose.components.IconBadge
import com.example.ovyu.compose.components.LoadingBox
import com.example.ovyu.compose.components.OvyuCard
import com.example.ovyu.compose.components.ScreenHeader
import com.example.ovyu.ui.theme.OvyuAmber
import com.example.ovyu.ui.theme.OvyuBlue
import com.example.ovyu.ui.theme.OvyuPurple
import com.example.ovyu.viewmodel.AppNotification
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.ListState
import com.example.ovyu.viewmodel.NotificationType
import com.example.ovyu.viewmodel.NotificationsViewModel

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNotificationClick: (NotificationType) -> Unit,
    viewModel: NotificationsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    NotificationsContent(state = state, onBack = onBack, onNotificationClick = onNotificationClick)
}

@Composable
fun NotificationsContent(
    state: ListState<AppNotification>,
    onBack: () -> Unit,
    onNotificationClick: (NotificationType) -> Unit
) {
    if (state.isLoading) {
        LoadingBox()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeader(
                title = "Notificações",
                subtitle = "Avisos com base nos seus dados",
                onBack = onBack
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (state.items.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.NotificationsNone,
                    title = "Tudo em dia",
                    message = "Consultas próximas, medicamentos de hoje e exames " +
                            "agendados aparecerão aqui."
                )
            }
        }

        items(state.items, key = { it.id }) { notification ->
            val (icon, tint) = when (notification.type) {
                NotificationType.APPOINTMENT -> Icons.Default.CalendarMonth to OvyuBlue
                NotificationType.MEDICATION -> Icons.Default.Medication to OvyuPurple
                NotificationType.EXAM -> Icons.Default.Science to OvyuAmber
            }

            OvyuCard(onClick = { onNotificationClick(notification.type) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBadge(icon = icon, tint = tint)

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = notification.title,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
