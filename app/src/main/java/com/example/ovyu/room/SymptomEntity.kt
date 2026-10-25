package com.example.ovyu.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sintomas",
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
data class SymptomEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "paciente_id")
    val patientId: Long,

    @ColumnInfo(name = "sintoma")
    val symptom: String,

    @ColumnInfo(name = "categoria")
    val category: String,

    @ColumnInfo(name = "intensidade")
    val intensity: Int,

    // Data no formato ISO (yyyy-MM-dd), por exemplo "2026-10-21".
    @ColumnInfo(name = "data_inicio")
    val startDate: String,

    @ColumnInfo(name = "duracao")
    val duration: String,

    @ColumnInfo(name = "localizacao_corpo")
    val bodyLocation: String = "",

    @ColumnInfo(name = "descricao")
    val description: String = "",

    @ColumnInfo(name = "criado_em")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "atualizado_em")
    val updatedAt: Long = System.currentTimeMillis()
)
