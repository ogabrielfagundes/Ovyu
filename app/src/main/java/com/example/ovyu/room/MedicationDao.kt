package com.example.ovyu.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Upsert
    suspend fun upsert(medication: MedicationEntity): Long

    @Query(
        """
        SELECT * FROM medicamentos
        WHERE paciente_id = :patientId
        ORDER BY ativo DESC, nome COLLATE NOCASE ASC
        """
    )
    fun observeAll(patientId: Long): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medicamentos WHERE id = :id AND paciente_id = :patientId LIMIT 1")
    suspend fun getById(id: Long, patientId: Long): MedicationEntity?

    @Query("UPDATE medicamentos SET ativo = :active WHERE id = :id AND paciente_id = :patientId")
    suspend fun setActive(id: Long, patientId: Long, active: Boolean)

    @Query("DELETE FROM medicamentos WHERE id = :id AND paciente_id = :patientId")
    suspend fun deleteById(id: Long, patientId: Long)
}
