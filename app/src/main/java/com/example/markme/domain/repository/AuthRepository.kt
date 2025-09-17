package com.example.markme.domain.repository

import com.example.markme.data.local.User

interface AuthRepository {

    suspend fun signIn(email: String, password: String): User?

    suspend fun signUp(user: User)

    suspend fun doesUserAlreadyExists(email: String,password: String): Boolean


}