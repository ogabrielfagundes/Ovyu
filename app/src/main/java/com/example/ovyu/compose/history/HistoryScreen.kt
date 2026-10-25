package com.example.ovyu.compose.history

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovyu.compose.components.EmptyState
import com.example.ovyu.compose.components.IconBadge
import com.example.ovyu.compose.components.LoadingBox
import com.example.ovyu.compose.components.OvyuCard
import com.example.ovyu.compose.components.ScreenHeader
import com.example.ovyu.compose.components.StatusChip
import com.example.ovyu.compose.health.intensityColor
import com.example.ovyu.ui.theme.OvyuAmber
import com.example.ovyu.ui.theme.OvyuBlue
import com.example.ovyu.ui.theme.OvyuPurple
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.HistoryItem
import com.example.ovyu.viewmodel.HistoryType
import com.example.ovyu.viewmodel.HistoryUiState
import com.example.ovyu.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    onItemClick: (HistoryType, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryContent(
        state = state,
        modifier = modifier,
        onFilterSelected = viewModel::setFilter,
        onItemClick = onItemClick
    )
}

@Composable
fun HistoryContent(
    state: HistoryUiState,
    onFilterSelected: (HistoryType?) -> Unit,
    onItemClick: (HistoryType, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeader(
                title = "Histórico",
                subtitle = "Tudo o que você registrou, do mais recente ao mais antigo"
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.filter == null,
                        onClick = { onFilterSelected(null) },
                        label = { Text("Tudo") }
                    )
                }
                items(HistoryType.entries) { type ->
                    FilterChip(
                        selected = state.filter == type,
                        onClick = { onFilterSelected(if (state.filter == type) null else type) },
                        label = { Text(type.label) }
                    )
                }
            }
        }

        if (state.isLoading) {
            item { LoadingBox(Modifier.height(160.dp)) }
        } else if (state.items.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "Nada por aqui ainda",
                    message = "Seus sintomas, consultas, exames e medicamentos aparecerão " +
                            "neste histórico."
                )
            }
        } else {
            items(state.items, key = { "${it.type}-${it.id}" }) { item ->
                HistoryCard(item = item, onClick = { onItemClick(item.type, item.id) })
            }
        }
    }
}

private fun HistoryType.icon(): ImageVector = when (this) {
    HistoryType.SYMPTOM -> Icons.Default.FavoriteBorder
    HistoryType.APPOINTMENT -> Icons.Default.CalendarMonth
    HistoryType.EXAM -> Icons.Default.Science
    HistoryType.MEDICATION -> Icons.Default.Medication
}

private fun HistoryType.tint(): Color = when (this) {
    HistoryType.SYMPTOM -> OvyuBlue
    HistoryType.APPOINTMENT -> OvyuPurple
    HistoryType.EXAM -> OvyuAmber
    HistoryType.MEDICATION -> OvyuBlue
}

@Composable
private fun HistoryCard(item: HistoryItem, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    OvyuCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = item.type.icon(), tint = item.type.tint())

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.type.label.dropLast(1)} • ${item.dateLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }

            if (item.badge != null) {
                val intensity = item.badge.substringBefore('/').toIntOrNull() ?: 0
                StatusChip(text = item.badge, color = intensityColor(intensity))
            }
        }
    }
}
