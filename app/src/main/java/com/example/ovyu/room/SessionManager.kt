package com.example.ovyu.room

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(
    name = "ovyu_session"
)

/**
 * Guarda no DataStore o ID do paciente logado.
 * É esse ID que identifica o dono de cada sintoma, medicamento, consulta etc.
 */
class SessionManager(
    private val context: Context
) {

    companion object {
        private val PATIENT_ID_KEY =
            longPreferencesKey("paciente_id")
    }

    // ID do paciente autenticado (null = ninguém logado).
    val patientId: Flow<Long?> =
        context.sessionDataStore.data.map { preferences ->
            preferences[PATIENT_ID_KEY]
        }

    // Lê o ID uma única vez (usado na hora de salvar um registro).
    suspend fun currentPatientId(): Long? = patientId.first()

    // Salva a sessão após o login ou cadastro.
    suspend fun saveSession(patientId: Long) {
        context.sessionDataStore.edit { preferences ->
            preferences[PATIENT_ID_KEY] = patientId
        }
    }

    // Encerra a sessão sem apagar os dados do paciente.
    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(PATIENT_ID_KEY)
        }
    }
}
