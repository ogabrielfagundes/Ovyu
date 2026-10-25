package com.example.ovyu.compose.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ovyu.room.ExamEntity
import com.example.ovyu.room.ExamStatus
import com.example.ovyu.ui.theme.OvyuAmber
import com.example.ovyu.ui.theme.OvyuBlue
import com.example.ovyu.ui.theme.OvyuGreen
import com.example.ovyu.util.toDisplayDate
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.ExamsUiState
import com.example.ovyu.viewmodel.ExamsViewModel

@Composable
fun ExamsScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: ExamsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ExamsContent(state = state, onBack = onBack, onAdd = onAdd, onEdit = onEdit)
}

@Composable
fun ExamsContent(
    state: ExamsUiState,
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
                        title = "Exames",
                        subtitle = "Agendamentos e resultados",
                        onBack = onBack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (state.pending.isEmpty() && state.done.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Science,
                            title = "Nenhum exame cadastrado",
                            message = "Cadastre seus exames e guarde os resultados.",
                            actionLabel = "Adicionar exame",
                            onAction = onAdd
                        )
                    }
                }

                if (state.pending.isNotEmpty()) {
                    item { SectionLabel("Agendados") }
                    items(state.pending, key = { "p-${it.id}" }) {
                        ExamCard(it, onClick = { onEdit(it.id) })
                    }
                }

                if (state.done.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionLabel("Realizados")
                    }
                    items(state.done, key = { "d-${it.id}" }) {
                        ExamCard(it, onClick = { onEdit(it.id) })
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
            text = { Text("Novo exame") }
        )
    }
}

@Composable
private fun ExamCard(exam: ExamEntity, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val done = exam.status == ExamStatus.DONE

    OvyuCard(onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Default.Science)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exam.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = exam.date.toDisplayDate() +
                                if (exam.lab.isNotBlank()) " • ${exam.lab}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusChip(
                    text = exam.status,
                    color = if (done) OvyuGreen else OvyuAmber
                )
            }

            if (exam.result.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Resultado: ${exam.result}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
