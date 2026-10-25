package com.example.ovyu.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {

    @Upsert
    suspend fun upsert(exam: ExamEntity): Long

    @Query("SELECT * FROM exames WHERE paciente_id = :patientId ORDER BY data DESC")
    fun observeAll(patientId: Long): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exames WHERE id = :id AND paciente_id = :patientId LIMIT 1")
    suspend fun getById(id: Long, patientId: Long): ExamEntity?

    @Query("DELETE FROM exames WHERE id = :id AND paciente_id = :patientId")
    suspend fun deleteById(id: Long, patientId: Long)
}
