package com.example.markme.domain.repository

import com.example.markme.data.local.Attendance
import com.example.markme.data.local.User
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {

    suspend fun getUserByQrCode(qrCode: String): User?

    suspend fun markAttendance(qrCode: String,userName: String): Boolean

    suspend fun getTodayAttendance(): Flow<List<Attendance>>
    suspend fun getAttendanceByDate(date: String): Flow<List<Attendance>>
    suspend fun getAllAttendanceDates(): Flow<List<String>>

    fun getAllUsers(): Flow<List<User>>
    suspend fun getAttendanceInRange(startDate: String, endDate: String): Flow<List<Attendance>>

    suspend fun getAttendanceForUser(userId: String, date: String): Attendance?

    suspend fun getUserById(userId: String): User?

    suspend fun getAllAttendance(): Flow<List<Attendance>>
}