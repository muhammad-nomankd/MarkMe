package com.example.markme.data.local

import androidx.room.TypeConverter
import com.example.markme.domain.model.AttendanceStatus
import com.example.markme.domain.model.UserRole


class ConvertersClass {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name


    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromAttendanceStatus(value: AttendanceStatus): String = value.name

    @TypeConverter
    fun toAttendanceStatus(value:String): AttendanceStatus = AttendanceStatus.valueOf(value)
}