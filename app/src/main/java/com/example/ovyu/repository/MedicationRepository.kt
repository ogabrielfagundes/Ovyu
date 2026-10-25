package com.example.ovyu.repository

import com.example.ovyu.room.MedicationDao
import com.example.ovyu.room.MedicationEntity
import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val dao: MedicationDao) {

    fun observeAll(patientId: Long): Flow<List<MedicationEntity>> = dao.observeAll(patientId)

    suspend fun getById(id: Long, patientId: Long): MedicationEntity? = dao.getById(id, patientId)

    suspend fun save(medication: MedicationEntity) {
        dao.upsert(medication)
    }

    suspend fun setActive(id: Long, patientId: Long, active: Boolean) =
        dao.setActive(id, patientId, active)

    suspend fun delete(id: Long, patientId: Long) = dao.deleteById(id, patientId)
}
