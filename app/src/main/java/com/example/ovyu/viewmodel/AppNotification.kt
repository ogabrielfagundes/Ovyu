package com.example.ovyu.viewmodel

import com.example.ovyu.room.AppointmentEntity
import com.example.ovyu.room.AppointmentStatus
import com.example.ovyu.room.ExamEntity
import com.example.ovyu.room.ExamStatus
import com.example.ovyu.room.MedicationEntity
import com.example.ovyu.room.timesList
import com.example.ovyu.util.friendly
import com.example.ovyu.util.toDisplayDate
import com.example.ovyu.util.toLocalDateOrNull
import com.example.ovyu.util.toLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class NotificationType { APPOINTMENT, MEDICATION, EXAM }

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String
)

/** Medicamento ativo e dentro do período de uso na data informada. */
fun MedicationEntity.isTakenOn(day: LocalDate): Boolean {
    if (!active) return false
    val start = startDate.toLocalDateOrNull()
    val end = endDate.toLocalDateOrNull()
    if (start != null && day.isBefore(start)) return false
    if (end != null && day.isAfter(end)) return false
    return true
}

/**
 * Gera os avisos do app a partir dos dados locais (sem push/servidor):
 * - consultas agendadas nas próximas 48 horas;
 * - medicamentos em uso hoje;
 * - exames agendados nos próximos 7 dias.
 */
fun buildNotifications(
    appointments: List<AppointmentEntity>,
    medications: List<MedicationEntity>,
    exams: List<ExamEntity>,
    now: LocalDateTime = LocalDateTime.now()
): List<AppNotification> {
    val today = now.toLocalDate()
    val result = mutableListOf<AppNotification>()

    appointments
        .filter { it.status == AppointmentStatus.SCHEDULED }
        .map { it to it.dateTime.toLocalDateTime() }
        .filter { (_, dt) -> !dt.isBefore(now) && ChronoUnit.HOURS.between(now, dt) <= 48 }
        .sortedBy { it.second }
        .forEach { (appointment, dt) ->
            result += AppNotification(
                id = "appointment-${appointment.id}",
                type = NotificationType.APPOINTMENT,
                title = "Consulta com ${appointment.doctor}",
                message = buildString {
                    append(dt.friendly(today))
                    if (appointment.specialty.isNotBlank()) append(" • ${appointment.specialty}")
                    if (appointment.location.isNotBlank()) append(" • ${appointment.location}")
                }
            )
        }

    medications
        .filter { it.isTakenOn(today) }
        .forEach { medication ->
            val times = medication.timesList()
            result += AppNotification(
                id = "medication-${medication.id}",
                type = NotificationType.MEDICATION,
                title = "Medicamento de hoje: ${medication.name}",
                message = buildString {
                    append(medication.dosage)
                    append(" • ")
                    append(
                        if (times.isNotEmpty()) "Horários: ${times.joinToString(", ")}"
                        else medication.frequency
                    )
                }
            )
        }

    exams
        .filter { it.status == ExamStatus.SCHEDULED }
        .mapNotNull { exam -> exam.date.toLocalDateOrNull()?.let { exam to it } }
        .filter { (_, date) -> !date.isBefore(today) && !date.isAfter(today.plusDays(7)) }
        .sortedBy { it.second }
        .forEach { (exam, date) ->
            result += AppNotification(
                id = "exam-${exam.id}",
                type = NotificationType.EXAM,
                title = "Exame: ${exam.name}",
                message = when (date) {
                    today -> "Hoje"
                    today.plusDays(1) -> "Amanhã"
                    else -> date.toString().toDisplayDate()
                } + if (exam.lab.isNotBlank()) " • ${exam.lab}" else ""
            )
        }

    return result
}
