package com.example.ovyu.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(patient: PatientEntity): Long

    @Query("SELECT * FROM pacientes WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun getByEmail(email: String): PatientEntity?

    @Query("SELECT * FROM pacientes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PatientEntity?

    // Observa o paciente: usado para validar a sessão e mostrar o nome.
    @Query("SELECT * FROM pacientes WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<PatientEntity?>

    @Query("UPDATE pacientes SET nome = :name WHERE id = :id")
    suspend fun updateName(id: Long, name: String)

    // Os registros do paciente são removidos em cascata (ForeignKey).
    @Query("DELETE FROM pacientes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM pacientes")
    suspend fun countPatients(): Int
}
