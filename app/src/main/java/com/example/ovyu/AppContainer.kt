package com.example.ovyu

import android.app.Application
import android.content.Context
import com.example.ovyu.repository.AppointmentRepository
import com.example.ovyu.repository.AuthRepository
import com.example.ovyu.repository.ExamRepository
import com.example.ovyu.repository.MedicationRepository
import com.example.ovyu.repository.SymptomRepository
import com.example.ovyu.room.OvyuDatabase
import com.example.ovyu.room.SessionManager

/**
 * Injeção de dependência manual (sem Hilt, para manter o MVP simples).
 * Criado uma única vez na Application e usado pelas factories dos ViewModels.
 */
class AppContainer(context: Context) {
    private val database = OvyuDatabase.getInstance(context)

    val sessionManager = SessionManager(context.applicationContext)

    val authRepository = AuthRepository(database.patientDao())
    val symptomRepository = SymptomRepository(database.symptomDao())
    val medicationRepository = MedicationRepository(database.medicationDao())
    val appointmentRepository = AppointmentRepository(database.appointmentDao())
    val examRepository = ExamRepository(database.examDao())
}

class OvyuApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
