package com.example.ovyu.room


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pacientes", indices = [Index(value = ["email"], unique = true)])
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "nome")
    val name: String,

    val email: String,

    @ColumnInfo(name = "senha_Hash")
    val passwordHash: String,

    /*@ColumnInfo(name = "senha_salt")
    val passwordSalt: String,*/

    @ColumnInfo(name = "criado_em")
    val createdAt: Long = System.currentTimeMillis()
)