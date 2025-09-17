package com.example.markme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [User::class, Attendance::class], version = 2, exportSchema = false
)
@TypeConverters(ConvertersClass::class)
abstract class MarkMeDatabase: RoomDatabase(){
    abstract fun userDao(): AuthDao
    abstract fun attendanceDao(): AttendanceDao
}