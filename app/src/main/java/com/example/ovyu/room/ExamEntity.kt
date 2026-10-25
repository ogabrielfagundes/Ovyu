package com.example.ovyu.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object ExamStatus {
    const val SCHEDULED = "Agendado"
    const val DONE = "Realizado"

    val all = listOf(SCHEDULED, DONE)
}

@Entity(
    tableName = "exames",
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
data class ExamEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "paciente_id")
    val patientId: Long,

    @ColumnInfo(name = "nome")
    val name: String,

    // Data no formato ISO (yyyy-MM-dd).
    @ColumnInfo(name = "data")
    val date: String,

    @ColumnInfo(name = "laboratorio")
    val lab: String = "",

    @ColumnInfo(name = "status")
    val status: String = ExamStatus.SCHEDULED,

    @ColumnInfo(name = "resultado")
    val result: String = "",

    @ColumnInfo(name = "criado_em")
    val createdAt: Long = System.currentTimeMillis()
)
