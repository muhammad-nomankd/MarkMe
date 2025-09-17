package com.example.markme.presentation.home.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType.Companion.Date
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.markme.data.local.Attendance
import com.example.markme.domain.model.AttendanceStatus
import com.example.markme.presentation.home.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceListScreen(
    onBackPressed: () -> Unit,paddingValues: PaddingValues, viewModel: AttendanceViewModel = hiltViewModel()
) {
    val attendanceList by viewModel.attendanceList.collectAsState(emptyList())

    var selectedDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadTodayAttendance()
        viewModel.loadAvailableDates()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Attendance Records",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    viewModel.exportToCsv(context, attendanceList)
                }) {
                Icon(Icons.Default.Share, contentDescription = "Export")
            }
        }

        // Date Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = selectedDate.ifEmpty { "Select Date" }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    selectedDate = ""
                    viewModel.loadTodayAttendance()
                }) {
                Text("Today")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Attendance List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(attendanceList) { attendance ->
                AttendanceCard(attendance = attendance)
            }
        }
    }

    // Android Date Picker Dialog trigger
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        LaunchedEffect(Unit) {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val selected =
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                    selectedDate = selected
                    viewModel.loadAttendanceByDate(selected)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
            showDatePicker = false
        }
    }
}

@Composable
fun AttendanceCard(attendance: Attendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = attendance.userName, style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = attendance.status.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (attendance.status) {
                        AttendanceStatus.PRESENT -> Color.Blue
                        AttendanceStatus.LATE -> Color.Yellow
                        AttendanceStatus.ABSENT -> Color.Red
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))


            Text(
                text = "Time: ${formatTimeMillis(attendance.timeIn.toLong())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Date: ${attendance.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatTimeMillis(timeInMillis: Long?): String {
    return if (timeInMillis != null && timeInMillis > 0) {
        try {
            SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(Date(timeInMillis))
        } catch (e: Exception) {
            "--:--:--" // fallback if parsing fails
        }
    } else {
        "--:--:--" // default for null or invalid values
    }
}
