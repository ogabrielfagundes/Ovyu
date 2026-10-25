package com.example.ovyu.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Base64
import com.example.ovyu.room.PatientDao
import com.example.ovyu.room.PatientEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class AuthRepository(
    private val patientDao: PatientDao
) {

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {

        val normalizedName = name.trim()
        val normalizedEmail = email.trim().lowercase()

        if (normalizedName.isBlank() ||
            normalizedEmail.isBlank() ||
            password.isBlank()
        ) {
            return@withContext AuthResult.Error("Preencha todos os campos.")
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(normalizedEmail).matches()
        ) {
            return@withContext AuthResult.Error("Informe um e-mail válido.")
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            return@withContext AuthResult.Error(
                "A senha deve ter pelo menos $MIN_PASSWORD_LENGTH caracteres."
            )
        }

        if (patientDao.getByEmail(normalizedEmail) != null) {
            return@withContext AuthResult.Error("Este e-mail já está cadastrado.")
        }

        val patient = PatientEntity(
            name = normalizedName,
            email = normalizedEmail,
            passwordHash = hashPassword(password)
        )

        try {
            val patientId = patientDao.insert(patient)
            AuthResult.Success(patient.copy(id = patientId))
        } catch (e: SQLiteConstraintException) {
            AuthResult.Error(
                "Não foi possível cadastrar. Verifique se o e-mail já existe."
            )
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {

        val normalizedEmail = email.trim().lowercase()

        if (normalizedEmail.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Informe o e-mail e a senha.")
        }

        val user = patientDao.getByEmail(normalizedEmail)
            ?: return@withContext AuthResult.Error("E-mail ou senha incorretos.")

        if (verifyPassword(password, user.passwordHash)) {
            AuthResult.Success(user)
        } else {
            AuthResult.Error("E-mail ou senha incorretos.")
        }
    }

    fun observePatient(id: Long): Flow<PatientEntity?> = patientDao.observeById(id)

    suspend fun updateName(id: Long, name: String) {
        patientDao.updateName(id, name.trim())
    }

    /** Apaga o paciente e, em cascata, todos os seus registros. */
    suspend fun deleteAccount(id: Long) {
        patientDao.deleteById(id)
    }

    private fun hashPassword(password: String): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val hash = pbkdf2(password, salt)

        // O salt é armazenado junto do hash, não junto da senha.
        return "${salt.toBase64()}:${hash.toBase64()}"
    }

    private fun verifyPassword(password: String, storedValue: String): Boolean {
        return try {
            val parts = storedValue.split(":")
            if (parts.size != 2) return false

            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val expectedHash = Base64.decode(parts[1], Base64.NO_WRAP)
            val actualHash = pbkdf2(password, salt)

            MessageDigest.isEqual(expectedHash, actualHash)
        } catch (e: Exception) {
            false
        }
    }

    private fun pbkdf2(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        return try {
            SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
        private const val ITERATIONS = 120_000
        private const val KEY_LENGTH = 256
    }
}

sealed class AuthResult {
    data class Success(val patient: PatientEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
