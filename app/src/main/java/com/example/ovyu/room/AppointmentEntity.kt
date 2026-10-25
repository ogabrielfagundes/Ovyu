package com.example.ovyu.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object AppointmentStatus {
    const val SCHEDULED = "Agendada"
    const val DONE = "Realizada"
    const val CANCELLED = "Cancelada"

    val all = listOf(SCHEDULED, DONE, CANCELLED)
}

@Entity(
    tableName = "consultas",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["paciente_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["paciente_id"])]
)
data class AppointmentEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "paciente_id")
    val patientId: Long,

    @ColumnInfo(name = "medico")
    val doctor: String,

    @ColumnInfo(name = "especialidade")
    val specialty: String,

    // Data e hora da consulta em milissegundos (fuso local do aparelho).
    @ColumnInfo(name = "data_hora")
    val dateTime: Long,

    @ColumnInfo(name = "local")
    val location: String = "",

    @ColumnInfo(name = "observacoes")
    val notes: String = "",

    @ColumnInfo(name = "status")
    val status: String = AppointmentStatus.SCHEDULED,

    @ColumnInfo(name = "criado_em")
    val createdAt: Long = System.currentTimeMillis()
)
