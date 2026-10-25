package com.example.ovyu.viewmodel

/** Nome do argumento de navegação usado nas telas de edição (-1 = novo registro). */
const val ID_ARG = "id"

/** Estado genérico de telas de lista. */
data class ListState<T>(
    val isLoading: Boolean = true,
    val items: List<T> = emptyList()
)

val symptomCategories = listOf(
    "Dor",
    "Febre",
    "Desconforto respiratório",
    "Sintomas digestivos",
    "Cansaço ou fraqueza",
    "Sintomas emocionais",
    "Outros"
)

val symptomDurations = listOf(
    "Há menos de 1 hora",
    "Entre 1 e 6 horas",
    "Entre 6 e 24 horas",
    "Entre 1 e 3 dias",
    "Entre 4 e 7 dias",
    "Há mais de 1 semana"
)

val medicationFrequencies = listOf(
    "1 vez ao dia",
    "2 vezes ao dia",
    "3 vezes ao dia",
    "A cada 8 horas",
    "A cada 12 horas",
    "Semanalmente",
    "Somente se necessário"
)
