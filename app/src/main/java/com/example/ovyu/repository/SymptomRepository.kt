package com.example.ovyu.repository

import com.example.ovyu.room.SymptomDao
import com.example.ovyu.room.SymptomEntity
import kotlinx.coroutines.flow.Flow

class SymptomRepository(private val dao: SymptomDao) {

    fun observeAll(patientId: Long): Flow<List<SymptomEntity>> = dao.observeAll(patientId)

    suspend fun getById(id: Long, patientId: Long): SymptomEntity? = dao.getById(id, patientId)

    suspend fun save(symptom: SymptomEntity) {
        dao.upsert(symptom)
    }

    suspend fun delete(id: Long, patientId: Long) = dao.deleteById(id, patientId)
}
