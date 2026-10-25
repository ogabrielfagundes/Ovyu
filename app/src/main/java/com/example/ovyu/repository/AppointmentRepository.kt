package com.example.ovyu.repository

import com.example.ovyu.room.AppointmentDao
import com.example.ovyu.room.AppointmentEntity
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(private val dao: AppointmentDao) {

    fun observeAll(patientId: Long): Flow<List<AppointmentEntity>> = dao.observeAll(patientId)

    suspend fun getById(id: Long, patientId: Long): AppointmentEntity? = dao.getById(id, patientId)

    suspend fun save(appointment: AppointmentEntity) {
        dao.upsert(appointment)
    }

    suspend fun delete(id: Long, patientId: Long) = dao.deleteById(id, patientId)
}
