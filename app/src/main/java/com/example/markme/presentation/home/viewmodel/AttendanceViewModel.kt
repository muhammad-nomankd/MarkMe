package com.example.markme.presentation.home.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.markme.data.local.Attendance
import com.example.markme.domain.repository.AttendanceRepository
import com.opencsv.CSVWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class AttendanceViewModel @Inject constructor(private val repository: AttendanceRepository): ViewModel() {

    private val _attendanceList = MutableStateFlow<List<Attendance>>(emptyList())
    val attendanceList: StateFlow<List<Attendance>> = _attendanceList.asStateFlow()

    private val _availableDates = MutableStateFlow<List<String>>(emptyList())
    val availableDates: StateFlow<List<String>> = _availableDates.asStateFlow()

    fun loadTodayAttendance() {
        viewModelScope.launch {
            repository.getTodayAttendance().collectLatest { list ->
                _attendanceList.value = list
            }
        }
    }

    fun loadAttendanceByDate(date: String) {
        viewModelScope.launch {
            repository.getAttendanceByDate(date).collectLatest { list ->
                _attendanceList.value = list
            }
        }
    }

    fun loadAvailableDates() {
        viewModelScope.launch {
            repository.getAllAttendanceDates().collectLatest { dates ->
                _availableDates.value = dates
            }
        }
    }

    fun exportToCsv(context: Context, list: List<Attendance>) {
        viewModelScope.launch {
            try {
                // Save file in app-specific external storage
                val file = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "attendance_${System.currentTimeMillis()}.csv"
                )

                // Write CSV
                val writer = FileWriter(file)
                val csvWriter = CSVWriter(writer)
                csvWriter.writeNext(arrayOf("Name", "Date", "TimeIn", "Status"))
                list.forEach { attendance ->
                    val timeIn = formatTimeMillis(attendance.timeIn.toLong())

                    val displayDate = formatDateForCsv(attendance.date)
                    csvWriter.writeNext(
                        arrayOf(
                            attendance.userName,
                            displayDate,
                            timeIn,
                            attendance.status.name
                        )
                    )
                }
                csvWriter.close()

                // Step 2: Get Uri with FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider", // must match manifest provider authority
                    file
                )

                // Step 3: Share Intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Launch chooser
                context.startActivity(
                    Intent.createChooser(shareIntent, "Share Attendance CSV")
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }


        }
    }

}

fun formatDateForCsv(dateStr: String?): String {
    return if (dateStr.isNullOrBlank()) {
        ""
    } else {
        try {
            val src = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dst = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            "'" + dst.format(src.parse(dateStr)!!)
        } catch (e: Exception) {
            "'" + dateStr
        }
    }
}

fun formatTimeMillis(timeInMillis: Long?): String {
    return if (timeInMillis != null && timeInMillis > 0) {
        try {
            SimpleDateFormat("hh:mm ", Locale.getDefault())
                .format(Date(timeInMillis))
        } catch (e: Exception) {
            "--:--:--" // fallback if parsing fails
        }
    } else {
        "--:--:--" // default for null or invalid values
    }
}
