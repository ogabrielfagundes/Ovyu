package com.example.ovyu.compose

import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.ovyu.viewmodel.ID_ARG

enum class Screen(val route: String) {
    // Fluxo de autenticação
    LOGIN("login"),
    SIGNUP("signup"),
    MAIN("main"),

    // Abas da barra inferior
    HOME("home"),
    HEALTH("health"),
    HISTORY("history"),
    PROFILE("profile"),

    // Sintomas (formulário de registro/edição)
    REGISTER("register"),

    // Medicamentos
    MEDICATIONS("medications"),
    MEDICATION_FORM("medication_form"),

    // Consultas
    APPOINTMENTS("appointments"),
    APPOINTMENT_FORM("appointment_form"),

    // Exames
    EXAMS("exams"),
    EXAM_FORM("exam_form"),

    // Avisos
    NOTIFICATIONS("notifications");

    /** Padrão de rota dos formulários, com id opcional: "register?id={id}". */
    val formRoute: String
        get() = "$route?$ID_ARG={$ID_ARG}"

    /** Rota para abrir o formulário vazio (id = null) ou editar o registro [id]. */
    fun withId(id: Long? = null): String = "$route?$ID_ARG=${id ?: -1L}"
}

val formArguments = listOf(
    navArgument(ID_ARG) {
        type = NavType.LongType
        defaultValue = -1L
    }
)
