package com.example.markme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Query("select * from users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    @Query("select * from attendance where date = :date order by timeIn")
    fun getAttendanceByDate(date: String): Flow<List<Attendance>>

    @Query("select * from attendance where userId = :userId and date = :date")
    suspend fun getUserAttendanceForDate(date: String, userId: String): Attendance?

    @Query("select DISTINCT date from attendance order by date desc")
    fun allAttendanceDates(): Flow<List<String>>

    @Query("select * from users where qrCode = :qrCode")
    suspend fun getUserByQrCode(qrCode: String): User?

    @Query("select * from attendance order by date desc, timeIn desc")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, timeIn DESC")
    fun getAttendanceInRange(startDate: String, endDate: String): Flow<List<Attendance>>
}