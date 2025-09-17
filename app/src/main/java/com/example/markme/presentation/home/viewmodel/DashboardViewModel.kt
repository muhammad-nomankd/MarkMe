package com.example.markme.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.markme.data.local.Attendance
import com.example.markme.domain.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _dashboardStats =
        MutableStateFlow<DashboardStats>(DashboardStats(0, 0, 0, 0, emptyList()))
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats

    private val _attendanceList = MutableStateFlow<List<Attendance>>(emptyList())
    val attendanceList: StateFlow<List<Attendance>> = _attendanceList

    init {
        loadDashboardStats()
        getAllAttendanceDesc()
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            try {
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = dateFormatter.format(Date())

                val todayAttendanceList = repository.getAttendanceByDate(today).first()
                val allUsersList = repository.getAllUsers().first()

                val presentToday = todayAttendanceList.size
                val totalUsers = allUsersList.size
                val absentToday = totalUsers - presentToday

                // Get attendance for last 7 days
                val cal = Calendar.getInstance()
                val weeklyAttendance = mutableListOf<DailyAttendance>()

                repeat(7) {
                    val date = dateFormatter.format(cal.time)
                    val attendanceCount = repository.getAttendanceByDate(date).first().size
                    weeklyAttendance.add(DailyAttendance(date, attendanceCount))
                    cal.add(Calendar.DAY_OF_MONTH, -1)
                }

                _dashboardStats.value = DashboardStats(
                    totalUsers = totalUsers,
                    presentToday = presentToday,
                    absentToday = absentToday,
                    attendanceRate = if (totalUsers > 0) (presentToday * 100) / totalUsers else 0,
                    weeklyAttendance = weeklyAttendance.reversed()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAllAttendanceDesc(){
        viewModelScope.launch {
            repository.getAllAttendance().collect {
                _attendanceList.value = it
            }
        }
    }
}

data class DashboardStats(
    val totalUsers: Int,
    val presentToday: Int,
    val absentToday: Int,
    val attendanceRate: Int,
    val weeklyAttendance: List<DailyAttendance>
)

data class DailyAttendance(
    val date: String, val count: Int
)