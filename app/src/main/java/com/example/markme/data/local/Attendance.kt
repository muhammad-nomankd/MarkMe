package com.example.markme.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.markme.domain.model.AttendanceStatus
import java.util.UUID
@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userName: String,
    val date: String,
    val timeIn: String,
    val timeOut: String? = null,
    val status: AttendanceStatus,

)
