package com.example.markme.di

import android.content.Context
import androidx.room.Room
import com.example.markme.data.local.AttendanceDao
import com.example.markme.data.local.AuthDao
import com.example.markme.data.local.MarkMeDatabase
import com.example.markme.data.local.SessionManager
import com.example.markme.data.repository.AttendanceRepositoryImp
import com.example.markme.data.repository.AuthRepositoryImp
import com.example.markme.domain.repository.AttendanceRepository
import com.example.markme.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AuthModule {

    @Provides
    @Singleton
    fun provideMarkMeDatabase(@ApplicationContext context: Context): MarkMeDatabase{
        return Room.databaseBuilder(
            context,
            MarkMeDatabase::class.java,
            "markme_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthDao(database: MarkMeDatabase): AuthDao{
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(authDao: AuthDao): AuthRepository{
        return AuthRepositoryImp(authDao)
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideAttendanceDao(database: MarkMeDatabase): AttendanceDao{
        return database.attendanceDao()
    }

    @Provides
    @Singleton
    fun provideAttendanceRepository(attendanceDao: AttendanceDao): AttendanceRepository{
        return AttendanceRepositoryImp(attendanceDao)
    }
}