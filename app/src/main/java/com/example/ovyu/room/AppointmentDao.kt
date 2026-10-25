package com.example.ovyu.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Upsert
    suspend fun upsert(appointment: AppointmentEntity): Long

    @Query("SELECT * FROM consultas WHERE paciente_id = :patientId ORDER BY data_hora ASC")
    fun observeAll(patientId: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM consultas WHERE id = :id AND paciente_id = :patientId LIMIT 1")
    suspend fun getById(id: Long, patientId: Long): AppointmentEntity?

    @Query("DELETE FROM consultas WHERE id = :id AND paciente_id = :patientId")
    suspend fun deleteById(id: Long, patientId: Long)
}
