package com.example.markme.data.repository

import com.example.markme.data.local.User
import com.example.markme.data.local.AuthDao
import com.example.markme.domain.repository.AuthRepository
import java.security.MessageDigest
import javax.inject.Inject

class AuthRepositoryImp @Inject constructor(private val userDao: AuthDao): AuthRepository {

    private fun normalizeEmail(email: String): String = email.trim().lowercase()

    private fun hashPassword(email: String, password: String): String {
        val salted = "$email:$password"
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(salted.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override suspend fun signIn(email: String, password: String): User? {
        val normalizedEmail = normalizeEmail(email)
        val user = userDao.getUserByEmail(normalizedEmail)
        if (user == null) return null
        val attemptedHash = hashPassword(normalizedEmail, password)
        return if (user.password == attemptedHash) user else null
    }

    override suspend fun signUp(user: User) {
        val normalizedEmail = normalizeEmail(user.email)
        val existing = userDao.getUserByEmail(normalizedEmail)
        if (existing != null) {
            throw IllegalArgumentException("An account with this email already exists")
        }
        val hashed = user.copy(
            email = normalizedEmail,
            password = hashPassword(normalizedEmail, user.password)
        )
        userDao.insertUser(hashed)
    }

    override suspend fun doesUserAlreadyExists(email: String, password: String): Boolean {
         return userDao.doesUserAccountAlreadyExists(email,password)
    }
}