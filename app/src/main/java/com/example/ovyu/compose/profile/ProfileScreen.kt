package com.example.ovyu.compose.profile

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovyu.compose.components.ConfirmDialog
import com.example.ovyu.compose.components.DangerTextButton
import com.example.ovyu.compose.components.IconBadge
import com.example.ovyu.compose.components.LoadingBox
import com.example.ovyu.compose.components.OvyuCard
import com.example.ovyu.compose.components.OvyuGradient
import com.example.ovyu.compose.components.ScreenHeader
import com.example.ovyu.compose.components.SectionLabel
import com.example.ovyu.util.format
import com.example.ovyu.util.toLocalDateTime
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.ProfileUiState
import com.example.ovyu.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileContent(
        state = state,
        modifier = modifier,
        onUpdateName = viewModel::updateName,
        onLogout = viewModel::logout,
        onDeleteAccount = viewModel::deleteAccount
    )
}

@Composable
fun ProfileContent(
    state: ProfileUiState,
    onUpdateName: (String) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditName by remember { mutableStateOf(false) }
    var showLogout by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    if (state.isLoading) {
        LoadingBox(modifier)
        return
    }

    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        ScreenHeader(title = "Perfil", subtitle = "Sua conta neste aparelho")

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar + dados
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(OvyuGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.name.trim().take(1).uppercase().ifEmpty { "?" },
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = state.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = state.email,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            if (state.createdAt > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Conta criada em ${state.createdAt.toLocalDateTime().toLocalDate().format()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = { showEditName = true }) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("Editar nome")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionLabel("Seus registros")
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CountTile(Icons.Default.FavoriteBorder, state.symptomCount, "Sintomas", Modifier.weight(1f))
            CountTile(Icons.Default.Medication, state.medicationCount, "Medicamentos", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CountTile(Icons.Default.CalendarMonth, state.appointmentCount, "Consultas", Modifier.weight(1f))
            CountTile(Icons.Default.Science, state.examCount, "Exames", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedButton(
            onClick = { showLogout = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Sair da conta", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        DangerTextButton(text = "Excluir conta e dados", onClick = { showDelete = true })

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "O Ovyu é um MVP acadêmico. Todos os dados ficam salvos somente neste aparelho.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showEditName) {
        EditNameDialog(
            currentName = state.name,
            onConfirm = {
                onUpdateName(it)
                showEditName = false
            },
            onDismiss = { showEditName = false }
        )
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("Sair da conta", fontWeight = FontWeight.Bold) },
            text = { Text("Você poderá entrar novamente com seu e-mail e senha. Seus dados continuam salvos.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogout = false
                    onLogout()
                }) { Text("Sair", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogout = false }) { Text("Cancelar") }
            }
        )
    }

    if (showDelete) {
        ConfirmDialog(
            title = "Excluir conta e dados",
            message = "Sua conta e todos os sintomas, medicamentos, consultas e exames serão " +
                    "apagados deste aparelho. Esta ação não pode ser desfeita.",
            confirmLabel = "Excluir tudo",
            onConfirm = {
                showDelete = false
                onDeleteAccount()
            },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
private fun CountTile(icon: ImageVector, count: Int, label: String, modifier: Modifier = Modifier) {
    OvyuCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = icon, size = 38.dp)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = count.toString(),
                    fontSize = 20.sp,
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
}

@Composable
private fun EditNameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar nome", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
