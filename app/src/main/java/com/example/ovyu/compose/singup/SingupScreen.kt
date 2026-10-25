package com.example.ovyu.compose.singup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovyu.R
import com.example.ovyu.repository.AuthRepository
import com.example.ovyu.ui.theme.OvyuTheme
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

private val OvyuBlueStart = Color(0xFF0072D6)
private val OvyuBlueEnd = Color(0xFF0033A0)

/** Versão com ViewModel: grava o paciente no Room e a sessão no DataStore. */
@Composable
fun SignupScreen(
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SignupContent(
        isLoading = state.isLoading,
        errorMessage = state.error,
        onSignupClick = viewModel::register,
        onLoginClick = {
            viewModel.clearError()
            onLoginClick()
        },
        onInputChanged = viewModel::clearError
    )
}

@Composable
fun SignupContent(
    isLoading: Boolean,
    errorMessage: String?,
    onSignupClick: (name: String, email: String, password: String) -> Unit,
    onLoginClick: () -> Unit,
    onInputChanged: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var acceptedTerms by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    var showTerms by rememberSaveable { mutableStateOf(false) }
    var showPrivacy by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun comingSoon(feature: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("$feature estará disponível em breve.")
        }
    }

    val passwordsMatch = password == confirmPassword
    val passwordTooShort = password.isNotEmpty() &&
            password.length < AuthRepository.MIN_PASSWORD_LENGTH
    val canSubmit = username.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            !passwordTooShort &&
            passwordsMatch &&
            acceptedTerms &&
            !isLoading

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.iconeovyu2),
                contentDescription = "Logo Ovyu",
                modifier = Modifier.height(120.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Crie sua conta!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(24.dp))

            OvyuTextField(
                value = username,
                onValueChange = {
                    username = it
                    onInputChanged()
                },
                placeholder = "Nome",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(Modifier.height(12.dp))

            OvyuTextField(
                value = email,
                onValueChange = {
                    email = it
                    onInputChanged()
                },
                placeholder = "E-mail",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(Modifier.height(12.dp))

            OvyuTextField(
                value = password,
                onValueChange = {
                    password = it
                    onInputChanged()
                },
                placeholder = "Senha",
                isPassword = true,
                passwordVisible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                isError = passwordTooShort,
                errorText = "Use pelo menos ${AuthRepository.MIN_PASSWORD_LENGTH} caracteres",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(Modifier.height(12.dp))

            OvyuTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    onInputChanged()
                },
                placeholder = "Confirmar senha",
                isPassword = true,
                passwordVisible = confirmVisible,
                onToggleVisibility = { confirmVisible = !confirmVisible },
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                errorText = "As senhas não coincidem",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(Modifier.height(16.dp))

            TermsCheckbox(
                checked = acceptedTerms,
                onCheckedChange = { acceptedTerms = it },
                onTermsClick = { showTerms = true },
                onPrivacyClick = { showPrivacy = true }
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            GradientButton(
                text = "Cadastrar",
                enabled = canSubmit,
                isLoading = isLoading,
                onClick = { onSignupClick(username.trim(), email.trim(), password) }
            )

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Já tem uma conta? ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Fazer login",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onLoginClick)
                )
            }

            Spacer(Modifier.height(16.dp))

            OrDivider()

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                SocialIcon(
                    iconRes = R.drawable.ic_google,
                    contentDescription = "Entrar com Google",
                    onClick = { comingSoon("O cadastro com Google") }
                )
                SocialIcon(
                    iconRes = R.drawable.ic_facebook,
                    contentDescription = "Entrar com Facebook",
                    onClick = { comingSoon("O cadastro com Facebook") }
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Entre com outra conta",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showTerms) {
        InfoDialog(
            title = "Termos de Uso",
            text = "O Ovyu é um aplicativo acadêmico (MVP) para organizar suas informações " +
                    "de saúde. Todos os dados ficam salvos apenas neste aparelho. " +
                    "O app não substitui uma avaliação médica.",
            onDismiss = { showTerms = false }
        )
    }

    if (showPrivacy) {
        InfoDialog(
            title = "Política de Privacidade",
            text = "Seus dados (cadastro, sintomas, medicamentos, consultas e exames) são " +
                    "armazenados localmente, no banco do aparelho, e não são enviados para " +
                    "nenhum servidor. Ao excluir a conta pelo Perfil, tudo é apagado.",
            onDismiss = { showPrivacy = false }
        )
    }
}

@Composable
private fun InfoDialog(title: String, text: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Entendi") } }
    )
}

@Composable
private fun OvyuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        placeholder = {
            Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        singleLine = true,
        shape = CircleShape,
        isError = isError,
        supportingText = if (isError && errorText != null) {
            { Text(errorText) }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        trailingIcon = if (isPassword && onToggleVisibility != null) {
            {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff
                        else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun TermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    val linkStyle = TextLinkStyles(
        style = SpanStyle(color = MaterialTheme.colorScheme.primary)
    )

    val text = buildAnnotatedString {
        append("Li e concordo com os ")
        withLink(LinkAnnotation.Clickable("terms", linkStyle) { onTermsClick() }) {
            append("Termos de Uso")
        }
        append(" e ")
        withLink(LinkAnnotation.Clickable("privacy", linkStyle) { onPrivacyClick() }) {
            append("Política de Privacidade")
        }
        append(".")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                uncheckedColor = MaterialTheme.colorScheme.primary,
                checkedColor = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brush = if (enabled || isLoading) {
        Brush.horizontalGradient(listOf(OvyuBlueStart, OvyuBlueEnd))
    } else {
        Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(brush, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(
                text = text,
                color = if (enabled) Color.White
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "OU",
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SocialIcon(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(iconRes),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick)
    )
}

@Preview(showBackground = true, name = "Claro")
@Composable
private fun SignupScreenLightPreview() {
    OvyuTheme(darkTheme = false) {
        SignupContent(
            isLoading = false,
            errorMessage = null,
            onSignupClick = { _, _, _ -> },
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Escuro")
@Composable
private fun SignupScreenDarkPreview() {
    OvyuTheme(darkTheme = true) {
        SignupContent(
            isLoading = false,
            errorMessage = null,
            onSignupClick = { _, _, _ -> },
            onLoginClick = {}
        )
    }
}
