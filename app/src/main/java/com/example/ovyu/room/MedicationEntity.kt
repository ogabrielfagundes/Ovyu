package com.example.ovyu.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicamentos",
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
data class MedicationEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "paciente_id")
    val patientId: Long,

    @ColumnInfo(name = "nome")
    val name: String,

    // Ex.: "500 mg", "1 comprimido"
    @ColumnInfo(name = "dosagem")
    val dosage: String,

    @ColumnInfo(name = "frequencia")
    val frequency: String,

    // Horários separados por vírgula, ex.: "08:00,20:00"
    @ColumnInfo(name = "horarios")
    val times: String = "",

    // Datas no formato ISO (yyyy-MM-dd). endDate vazio = uso contínuo.
    @ColumnInfo(name = "data_inicio")
    val startDate: String,

    @ColumnInfo(name = "data_fim")
    val endDate: String = "",

    @ColumnInfo(name = "observacoes")
    val notes: String = "",

    @ColumnInfo(name = "ativo")
    val active: Boolean = true,

    @ColumnInfo(name = "criado_em")
    val createdAt: Long = System.currentTimeMillis()
)

// Horários como lista (fora da entidade para o Room não tratar como coluna).
fun MedicationEntity.timesList(): List<String> =
    times.split(",").map { it.trim() }.filter { it.isNotEmpty() }
