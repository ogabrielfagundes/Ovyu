package com.example.ovyu.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ovyu.compose.appointment.AppointmentFormScreen
import com.example.ovyu.compose.appointment.AppointmentsScreen
import com.example.ovyu.compose.exam.ExamFormScreen
import com.example.ovyu.compose.exam.ExamsScreen
import com.example.ovyu.compose.health.HealthScreen
import com.example.ovyu.compose.history.HistoryScreen
import com.example.ovyu.compose.home.HomeScreen
import com.example.ovyu.compose.medication.MedicationFormScreen
import com.example.ovyu.compose.medication.MedicationsScreen
import com.example.ovyu.compose.notification.NotificationsScreen
import com.example.ovyu.compose.profile.ProfileScreen
import com.example.ovyu.compose.register.RegisterScreen
import com.example.ovyu.ui.theme.OvyuTheme
import com.example.ovyu.viewmodel.HistoryType
import com.example.ovyu.viewmodel.NotificationType

/** Rotas que mostram a barra de navegação inferior. */
private val tabRoutes = setOf(
    Screen.HOME.route,
    Screen.HEALTH.route,
    Screen.HISTORY.route,
    Screen.PROFILE.route
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                // As telas de lista e formulário ocupam a tela inteira.
                if (currentRoute in tabRoutes) {
                    MainBottomNavigation(navController, currentRoute)
                }
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .consumeWindowInsets(contentPadding)
            ) {
                MainContentScreen(navController)
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val router: Screen
)

@Composable
fun MainBottomNavigation(navController: NavController, currentRoute: String?) {
    val navigationsItems = listOf(
        NavigationItem("Início", Icons.Default.Home, Screen.HOME),
        NavigationItem("Saúde", Icons.Default.MonitorHeart, Screen.HEALTH),
        NavigationItem("Histórico", Icons.Default.History, Screen.HISTORY),
        NavigationItem("Perfil", Icons.Default.Person, Screen.PROFILE),
    )

    NavigationBar(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        navigationsItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.router.route,
                onClick = {
                    if (currentRoute != item.router.route) {
                        navController.navigateToTab(item.router)
                    }
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.title)
                },
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedIconColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

/** Troca de aba mantendo o estado de cada uma. */
private fun NavController.navigateToTab(screen: Screen) {
    navigate(screen.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun MainContentScreen(navController: NavHostController) {
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = Screen.HOME.route
    ) {
        // ------------------------------------------------------------ Abas
        composable(Screen.HOME.route) {
            HomeScreen(
                onNotificationsClick = { navController.navigate(Screen.NOTIFICATIONS.route) },
                onHealthOverviewClick = { navController.navigateToTab(Screen.HEALTH) },
                onSymptomsClick = { navController.navigate(Screen.REGISTER.withId()) },
                onMedicationsClick = { navController.navigate(Screen.MEDICATIONS.route) },
                onAppointmentsClick = { navController.navigate(Screen.APPOINTMENTS.route) },
                onExamsClick = { navController.navigate(Screen.EXAMS.route) },
                onAppointmentClick = { id ->
                    navController.navigate(Screen.APPOINTMENT_FORM.withId(id))
                }
            )
        }
        composable(Screen.HEALTH.route) {
            HealthScreen(
                onRegisterSymptom = { navController.navigate(Screen.REGISTER.withId()) },
                onSymptomsClick = { navController.navigateToTab(Screen.HISTORY) },
                onMedicationsClick = { navController.navigate(Screen.MEDICATIONS.route) },
                onAppointmentsClick = { navController.navigate(Screen.APPOINTMENTS.route) },
                onExamsClick = { navController.navigate(Screen.EXAMS.route) },
                onAppointmentClick = { id ->
                    navController.navigate(Screen.APPOINTMENT_FORM.withId(id))
                },
                onSymptomClick = { id -> navController.navigate(Screen.REGISTER.withId(id)) }
            )
        }
        composable(Screen.HISTORY.route) {
            HistoryScreen(
                onItemClick = { type, id ->
                    val route = when (type) {
                        HistoryType.SYMPTOM -> Screen.REGISTER
                        HistoryType.APPOINTMENT -> Screen.APPOINTMENT_FORM
                        HistoryType.EXAM -> Screen.EXAM_FORM
                        HistoryType.MEDICATION -> Screen.MEDICATION_FORM
                    }
                    navController.navigate(route.withId(id))
                }
            )
        }
        composable(Screen.PROFILE.route) {
            ProfileScreen()
        }

        // -------------------------------------------------------- Sintomas
        composable(Screen.REGISTER.formRoute, arguments = formArguments) {
            RegisterScreen(onBack = back)
        }

        // ---------------------------------------------------- Medicamentos
        composable(Screen.MEDICATIONS.route) {
            MedicationsScreen(
                onBack = back,
                onAdd = { navController.navigate(Screen.MEDICATION_FORM.withId()) },
                onEdit = { id -> navController.navigate(Screen.MEDICATION_FORM.withId(id)) }
            )
        }
        composable(Screen.MEDICATION_FORM.formRoute, arguments = formArguments) {
            MedicationFormScreen(onBack = back)
        }

        // ---------------------------------------------------------- Consultas
        composable(Screen.APPOINTMENTS.route) {
            AppointmentsScreen(
                onBack = back,
                onAdd = { navController.navigate(Screen.APPOINTMENT_FORM.withId()) },
                onEdit = { id -> navController.navigate(Screen.APPOINTMENT_FORM.withId(id)) }
            )
        }
        composable(Screen.APPOINTMENT_FORM.formRoute, arguments = formArguments) {
            AppointmentFormScreen(onBack = back)
        }

        // ------------------------------------------------------------ Exames
        composable(Screen.EXAMS.route) {
            ExamsScreen(
                onBack = back,
                onAdd = { navController.navigate(Screen.EXAM_FORM.withId()) },
                onEdit = { id -> navController.navigate(Screen.EXAM_FORM.withId(id)) }
            )
        }
        composable(Screen.EXAM_FORM.formRoute, arguments = formArguments) {
            ExamFormScreen(onBack = back)
        }

        // ------------------------------------------------------ Notificações
        composable(Screen.NOTIFICATIONS.route) {
            NotificationsScreen(
                onBack = back,
                onNotificationClick = { type ->
                    val target = when (type) {
                        NotificationType.APPOINTMENT -> Screen.APPOINTMENTS
                        NotificationType.MEDICATION -> Screen.MEDICATIONS
                        NotificationType.EXAM -> Screen.EXAMS
                    }
                    navController.navigate(target.route)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LightBottomNavPreview() {
    OvyuTheme(darkTheme = false) {
        MainBottomNavigation(rememberNavController(), Screen.HOME.route)
    }
}

@Preview(showBackground = true)
@Composable
fun DarkBottomNavPreview() {
    OvyuTheme(darkTheme = true) {
        MainBottomNavigation(rememberNavController(), Screen.HOME.route)
    }
}
