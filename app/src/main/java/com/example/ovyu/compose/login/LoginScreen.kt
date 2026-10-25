package com.example.ovyu.compose.login

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ovyu.R
import com.example.ovyu.ui.theme.OvyuBlue
import com.example.ovyu.ui.theme.OvyuDarkBlue
import com.example.ovyu.ui.theme.OvyuPurple
import com.example.ovyu.ui.theme.OvyuTextBlue
import com.example.ovyu.ui.theme.OvyuTheme
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/** Versão com ViewModel: ao logar, a sessão é salva e o app navega sozinho para a Home. */
@Composable
fun LoginScreen(
    onCreateAccount: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LoginContent(
        isLoading = state.isLoading,
        errorMessage = state.error,
        onLogin = viewModel::login,
        onCreateAccount = {
            viewModel.clearError()
            onCreateAccount()
        },
        onInputChanged = viewModel::clearError
    )
}

@Composable
fun LoginContent(
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (email: String, password: String) -> Unit,
    onCreateAccount: () -> Unit,
    onInputChanged: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Recursos que dependem de internet/serviços externos ficam fora do MVP local.
    fun comingSoon(feature: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("$feature estará disponível em breve.")
        }
    }

    Scaffold(
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logotipo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .padding(start = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iconeovyu2),
                        contentDescription = "Logo Ovyu"
                    )
                }

                Text(
                    text = "Ovyu",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(OvyuBlue, OvyuDarkBlue, OvyuPurple)
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Bem-vindo de volta!",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Campo de e-mail
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = false
                    onInputChanged()
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "E-mail",
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        fontSize = 19.sp
                    )
                },
                singleLine = true,
                isError = emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(50.dp),
                colors = loginFieldColors()
            )

            if (emailError) {
                FieldError("Informe seu e-mail.")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Campo de senha
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = false
                    onInputChanged()
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Senha",
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        fontSize = 19.sp
                    )
                },
                singleLine = true,
                isError = passwordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (passwordVisible) {
                                "Ocultar senha"
                            } else {
                                "Mostrar senha"
                            },
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                },
                shape = RoundedCornerShape(50.dp),
                colors = loginFieldColors()
            )

            if (passwordError) {
                FieldError("Informe sua senha.")
            }

            // Recuperação de senha (indisponível no MVP offline)
            TextButton(
                onClick = { comingSoon("A recuperação de senha") },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 2.dp)
            ) {
                Text(
                    text = "Esqueceu sua senha?",
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botão Entrar
            Button(
                onClick = {
                    emailError = email.isBlank()
                    passwordError = password.isBlank()

                    if (!emailError && !passwordError) {
                        onLogin(email.trim(), password)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OvyuBlue,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Entrar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cadastro
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ainda não tem uma conta?",
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant
                )

                TextButton(onClick = onCreateAccount) {
                    Text(
                        text = "Criar conta",
                        fontSize = 11.sp,
                        color = OvyuBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Separador "ou"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = colorScheme.outline
                )

                Text(
                    text = "ou",
                    modifier = Modifier.padding(horizontal = 10.dp),
                    fontSize = 13.sp,
                    color = OvyuBlue
                )

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Login social (provisório, indisponível no MVP)
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "G",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClick = { comingSoon("O login com Google") }
                        )
                        .semantics { contentDescription = "Entrar com Google" },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4285F4),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3D5A98))
                        .clickable(
                            role = Role.Button,
                            onClick = { comingSoon("O login com Facebook") }
                        )
                        .semantics { contentDescription = "Entrar com Facebook" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "f",
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Entre com outra conta",
                fontSize = 11.sp,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = OvyuDarkBlue,
    unfocusedBorderColor = OvyuTextBlue,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    errorContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
)

@Composable
private fun FieldError(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    OvyuTheme {
        LoginContent(
            isLoading = false,
            errorMessage = null,
            onLogin = { _, _ -> },
            onCreateAccount = {}
        )
    }
}
