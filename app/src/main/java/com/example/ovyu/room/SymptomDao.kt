package com.example.ovyu.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {

    // Insere (id = 0) ou atualiza (id existente).
    @Upsert
    suspend fun upsert(symptom: SymptomEntity): Long

    // Sintomas do paciente logado, do mais recente ao mais antigo.
    @Query(
        """
        SELECT * FROM sintomas
        WHERE paciente_id = :patientId
        ORDER BY data_inicio DESC, criado_em DESC
        """
    )
    fun observeAll(patientId: Long): Flow<List<SymptomEntity>>

    @Query("SELECT * FROM sintomas WHERE id = :symptomId AND paciente_id = :patientId LIMIT 1")
    suspend fun getById(symptomId: Long, patientId: Long): SymptomEntity?

    @Query("DELETE FROM sintomas WHERE id = :symptomId AND paciente_id = :patientId")
    suspend fun deleteById(symptomId: Long, patientId: Long)

    @Query("SELECT COUNT(*) FROM sintomas WHERE paciente_id = :patientId")
    suspend fun count(patientId: Long): Int
}
