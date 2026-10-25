package com.example.ovyu.repository

import com.example.ovyu.room.ExamDao
import com.example.ovyu.room.ExamEntity
import kotlinx.coroutines.flow.Flow

class ExamRepository(private val dao: ExamDao) {

    fun observeAll(patientId: Long): Flow<List<ExamEntity>> = dao.observeAll(patientId)

    suspend fun getById(id: Long, patientId: Long): ExamEntity? = dao.getById(id, patientId)

    suspend fun save(exam: ExamEntity) {
        dao.upsert(exam)
    }

    suspend fun delete(id: Long, patientId: Long) = dao.deleteById(id, patientId)
}
