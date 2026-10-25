package com.example.ovyu.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

val PtBr: Locale = Locale.forLanguageTag("pt-BR")

val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", PtBr)
val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", PtBr)
val DayMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd 'de' MMM", PtBr)
val MonthShortFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", PtBr)

// ---- Datas salvas como texto ISO (yyyy-MM-dd) ----

fun String.toLocalDateOrNull(): LocalDate? =
    if (isBlank()) null else runCatching { LocalDate.parse(this) }.getOrNull()

fun LocalDate.format(): String = format(DateFormatter)

/** "2026-10-21" -> "21/10/2026" (ou o texto original se for inválido). */
fun String.toDisplayDate(): String = toLocalDateOrNull()?.format() ?: this

// ---- Data/hora salva como milissegundos (fuso do aparelho) ----

fun Long.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun LocalDate.startOfDayMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

// ---- Conversões para o DatePicker do Material (que trabalha em UTC) ----

fun LocalDate.toPickerMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun Long.fromPickerMillis(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

// ---- Horários "HH:mm" ----

fun LocalTime.format(): String = format(TimeFormatter)

/** Texto amigável: "Hoje às 14:30", "Amanhã às 09:00" ou "25/10/2026 às 10:00". */
fun LocalDateTime.friendly(today: LocalDate = LocalDate.now()): String {
    val day = when (toLocalDate()) {
        today -> "Hoje"
        today.plusDays(1) -> "Amanhã"
        else -> toLocalDate().format()
    }
    return "$day às ${toLocalTime().format()}"
}
