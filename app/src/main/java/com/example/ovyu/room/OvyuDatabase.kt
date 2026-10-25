package com.example.ovyu.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PatientEntity::class,
        SymptomEntity::class,
        MedicationEntity::class,
        AppointmentEntity::class,
        ExamEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OvyuDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun symptomDao(): SymptomDao
    abstract fun medicationDao(): MedicationDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun examDao(): ExamDao

    companion object {

        @Volatile
        private var instance: OvyuDatabase? = null

        fun getInstance(context: Context): OvyuDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also {
                    instance = it
                }
            }
        }

        private fun buildDatabase(context: Context): OvyuDatabase {
            return Room.databaseBuilder(
                context,
                OvyuDatabase::class.java,
                "ovyu_app"
            )
                // MVP acadêmico: se o schema mudar, o banco é recriado.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
}
