package com.example.ovyu.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ovyu.R
import com.example.ovyu.compose.login.LoginScreen
import com.example.ovyu.compose.singup.SignupScreen
import com.example.ovyu.ui.theme.OvyuBlue
import com.example.ovyu.viewmodel.AppViewModelProvider
import com.example.ovyu.viewmodel.SessionState
import com.example.ovyu.viewmodel.SessionViewModel

@Composable
fun OvyuApp(
    sessionViewModel: SessionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val state = sessionState

        if (state is SessionState.Loading) {
            // Lendo a sessão salva no DataStore
            SplashScreen()
        } else {
            // A tela inicial é definida uma única vez: Home se já houver sessão, senão Login.
            val startDestination = remember {
                if (state is SessionState.LoggedIn) Screen.MAIN.route else Screen.LOGIN.route
            }

            OvyuNavHost(navController = navController, startDestination = startDestination)

            // Login/cadastro gravam a sessão -> vai para a Home.
            // Logout (ou conta apagada) limpa a sessão -> volta para o Login.
            LaunchedEffect(state) {
                val current = navController.currentDestination?.route ?: return@LaunchedEffect
                val clearAll: androidx.navigation.NavOptionsBuilder.() -> Unit = {
                    popUpTo(navController.graph.id) { inclusive = true }
                }

                when (state) {
                    is SessionState.LoggedIn ->
                        if (current != Screen.MAIN.route) {
                            navController.navigate(Screen.MAIN.route, clearAll)
                        }

                    SessionState.LoggedOut ->
                        if (current == Screen.MAIN.route) {
                            navController.navigate(Screen.LOGIN.route, clearAll)
                        }

                    SessionState.Loading -> Unit
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.iconeovyu2),
            contentDescription = "Logo Ovyu",
            modifier = Modifier.size(110.dp)
        )
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(28.dp),
            color = OvyuBlue,
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun OvyuNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.LOGIN.route) {
            LoginScreen(
                onCreateAccount = { navController.navigate(Screen.SIGNUP.route) }
            )
        }
        composable(Screen.SIGNUP.route) {
            SignupScreen(
                onLoginClick = { navController.popBackStack() }
            )
        }
        composable(Screen.MAIN.route) {
            MainScreen()
        }
    }
}
