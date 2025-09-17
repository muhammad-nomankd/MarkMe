package com.example.markme.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.markme.domain.model.UserRole
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val fullName: String,
    val email: String,
    val role: UserRole,
    val password: String,
    val qrCode: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis()
    )
