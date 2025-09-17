package com.example.markme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthDao {

    @Insert
    suspend fun insertUser(user:User)

    // Legacy query (unused after hashing implemented)
    @Query("select * from users where email = :email and password = :password")
    suspend fun signIn(email:String, password: String): User?

    // Case-insensitive email lookup to prevent duplicates on different casing
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("select * from users where role = :role")
    fun getUsers(role: String): Flow<List<User>>

    @Query("SELECT * FROM users WHERE role = 'ADMIN' LIMIT 1")
    suspend fun getAdmin(): User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): User?

    @Query("SELECT EXISTS(SELECT * FROM users WHERE email = :email AND password = :password)")
    suspend fun doesUserAccountAlreadyExists(email:String, password: String): Boolean

}