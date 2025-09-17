package com.example.markme.presentation.home.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.markme.data.local.Attendance
import com.example.markme.data.local.User
import com.example.markme.data.local.SessionManager
import com.example.markme.domain.repository.AttendanceRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UserDashboardViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    init {
        // Ensure the current user is loaded as soon as the ViewModel is created
        loadCurrentUserFromSession()
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userAttendance = MutableStateFlow<List<Attendance>>(emptyList())
    val userAttendance: StateFlow<List<Attendance>> = _userAttendance.asStateFlow()

    private val _attendanceStats = MutableStateFlow<UserAttendanceStats?>(null)
    val attendanceStats: StateFlow<UserAttendanceStats?> = _attendanceStats.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setCurrentUser(user: User) {
        viewModelScope.launch {
            try {
                _currentUser.value = user
                loadUserData(user.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    fun loadCurrentUserFromSession() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId()
                if (userId != null) {
                    val user = repository.getUserById(userId)
                    if (user != null) {
                        setCurrentUser(user)
                    }
                }
            } catch (e: Exception) {
                Log.d("UserDashboardViewModel", "Error loading current user: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun generateQrCode(qrCode: String): Bitmap? {
        if (qrCode.isBlank()) {
            return null
        }
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(qrCode, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y,
                        if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    )
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = dateFormatter.format(Date())

                // Load recent attendance history (last 30 days, take latest 10 for user)
                val cal = Calendar.getInstance()
                val endDate = dateFormatter.format(cal.time)
                cal.add(Calendar.DAY_OF_MONTH, -30)
                val startDate = dateFormatter.format(cal.time)

                val rangeList = repository.getAttendanceInRange(startDate, endDate).first()
                val userList = rangeList.filter { it.userId == userId }
                    .sortedWith(compareBy<Attendance>({ it.date }, { it.timeIn })).takeLast(10)
                _userAttendance.value = userList.reversed()

                // Compute stats
                val todayAttendance = repository.getAttendanceForUser(userId, today)
                val isMarkedToday = todayAttendance != null

                // Total days present in last 30 days
                val totalDaysPresent = userList.count()

                // Current streak (consecutive days up to today)
                var streak = 0
                val calStreak = Calendar.getInstance()
                while (true) {
                    val d = dateFormatter.format(calStreak.time)
                    val att = repository.getAttendanceForUser(userId, d)
                    if (att != null) {
                        streak++
                        calStreak.add(Calendar.DAY_OF_MONTH, -1)
                    } else {
                        break
                    }
                }

                // Monthly attendance rate
                val monthCal = Calendar.getInstance()
                val currentMonth = monthCal.get(Calendar.MONTH)
                val currentYear = monthCal.get(Calendar.YEAR)
                monthCal.set(Calendar.DAY_OF_MONTH, 1)
                val monthStart = dateFormatter.format(monthCal.time)
                val daysPassed = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) // 1..today
                val monthList =
                    repository.getAttendanceInRange(monthStart, endDate).first().filter {
                        it.userId == userId && isSameMonth(
                            it.date, currentMonth, currentYear, dateFormatter
                        )
                    }
                val monthlyRate = (monthList.size * 100) / daysPassed

                _attendanceStats.value = UserAttendanceStats(
                    totalDaysPresent = totalDaysPresent,
                    isMarkedToday = isMarkedToday,
                    currentStreak = streak,
                    monthlyAttendanceRate = monthlyRate
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun isSameMonth(
        dateStr: String, month: Int, year: Int, sdf: SimpleDateFormat
    ): Boolean {
        return try {
            val parsed = sdf.parse(dateStr) ?: return false
            val c = Calendar.getInstance().apply { time = parsed }
            c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year
        } catch (_: Exception) {
            false
        }
    }

    fun refreshData() {
        _currentUser.value?.let { user ->
            loadUserData(user.id)
            setCurrentUser(user)
        }
    }

    fun shareQrCode(context: Context) {
        _currentUser.value?.let { user ->
            viewModelScope.launch {
                try {
                    val primaryText = user.qrCode
                    val fallbackText = "${user.id}|${user.email}"
                    val qrBitmap =
                        if (primaryText.isNotBlank()) generateQrCode(primaryText) else generateQrCode(
                            fallbackText
                        )
                    qrBitmap?.let { bitmap ->
                        doShareQrImage(context, bitmap, user.fullName)
                    }
                } catch (e: Exception) {
                    // Handle sharing error
                }
            }
        }
    }

    private fun doShareQrImage(context: Context, bitmap: Bitmap, userName: String) {
        try {
            val file =
                File(context.cacheDir, "qr_${'$'}userName_${'$'}{System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            val uri = FileProvider.getUriForFile(
                context, "${'$'}{context.packageName}.fileprovider", file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "QR Code for ${'$'}userName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
        } catch (_: Exception) {
        }
    }
}

data class UserAttendanceStats(
    val totalDaysPresent: Int,
    val isMarkedToday: Boolean,
    val currentStreak: Int,
    val monthlyAttendanceRate: Int
)