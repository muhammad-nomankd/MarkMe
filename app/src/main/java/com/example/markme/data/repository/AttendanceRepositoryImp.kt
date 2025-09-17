package com.example.markme.data.repository

import com.example.markme.data.local.Attendance
import com.example.markme.data.local.AttendanceDao
import com.example.markme.data.local.User
import com.example.markme.domain.model.AttendanceStatus
import com.example.markme.domain.repository.AttendanceRepository
import com.example.markme.utils.toFormatedString
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceRepositoryImp @Inject constructor(private val attendanceDao: AttendanceDao) :
    AttendanceRepository {
    override suspend fun getUserByQrCode(qrCode: String): User? {
       val user =  attendanceDao.getUserByQrCode(qrCode)
        return user
    }

    override suspend fun markAttendance(qrCode: String, userName: String): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val existingAttendance = attendanceDao.getUserAttendanceForDate(today, qrCode)

        if (existingAttendance == null) {
            val attendance = Attendance(
                userId = qrCode,
                userName = userName,
                date = today,
                timeIn = System.currentTimeMillis().toString(),
                status = AttendanceStatus.PRESENT
            )
            attendanceDao.insertAttendance(attendance)
            return true
        }
        return false
    }

    override suspend fun getTodayAttendance(): Flow<List<Attendance>> {
        val today = Date().toFormatedString(Date())
        return attendanceDao.getAttendanceByDate(today)
    }

    override suspend fun getAttendanceByDate(date: String): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceByDate(date)
    }

    override suspend fun getAllAttendanceDates(): Flow<List<String>> {
        return attendanceDao.allAttendanceDates()
    }

    override fun getAllUsers(): Flow<List<User>> {
        return attendanceDao.getAllUsers()
    }

    override suspend fun getAttendanceInRange(
        startDate: String, endDate: String
    ): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceInRange(startDate, endDate)
    }

    override suspend fun getAttendanceForUser(
        userId: String, date: String
    ): Attendance? {
        return attendanceDao.getUserAttendanceForDate(date, userId)
    }

    override suspend fun getUserById(userId: String): User? {
        return attendanceDao.getUserById(userId)
    }

    override suspend fun getAllAttendance(): Flow<List<Attendance>> {
       return attendanceDao.getAllAttendance()
    }
}