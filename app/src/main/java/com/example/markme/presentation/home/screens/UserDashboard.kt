package com.example.markme.presentation.home.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.markme.data.local.Attendance
import com.example.markme.data.local.User
import com.example.markme.domain.model.AttendanceStatus
import com.example.markme.presentation.home.viewmodel.UserDashboardViewModel
import com.example.markme.presentation.home.viewmodel.formatTimeMillis
import com.example.markme.utils.generateQrCode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserDashboard(
    viewModel: UserDashboardViewModel = hiltViewModel(),
    currentUser: User?,
    onLogout: () -> Unit,
    paddingValues: PaddingValues
) {
    val userAttendance by viewModel.userAttendance.collectAsStateWithLifecycle(emptyList())
    val attendanceStats by viewModel.attendanceStats.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showFullScreenQr by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(currentUser) {
        viewModel.refreshData()
        if (currentUser == null) {
            viewModel.loadCurrentUserFromSession()
        }
        Log.d("UserDashboard", "${currentUser?.fullName} - ${currentUser?.email}")
        currentUser?.let { user ->
            viewModel.setCurrentUser(user)
            qrBitmap = generateQrCode(user.qrCode)
        }
    }

    if (showFullScreenQr) {
        FullScreenQrDialog(
            qrBitmap = qrBitmap,
            userName = currentUser?.fullName.orEmpty(),
            userEmail = currentUser?.email.orEmpty(),
            onDismiss = { showFullScreenQr = false },
            onShare = { viewModel.shareQrCode(context) })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome + Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Welcome back ${currentUser?.fullName}", style = MaterialTheme.typography.titleLarge)
            }
            Row {
                IconButton(onClick = { viewModel.refreshData() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Quick Status
        Card(
            modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = if (attendanceStats?.isMarkedToday == true) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (attendanceStats?.isMarkedToday == true) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (attendanceStats?.isMarkedToday == true) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        if (attendanceStats?.isMarkedToday == true) "Attendance Marked ✅" else "Not Marked Yet ⏰",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (attendanceStats?.isMarkedToday == true) "You’re good for today" else "Please show your QR code",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Stats
        attendanceStats?.let { stats ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(180.dp)
            ) {
                item {
                    UserStatCard(
                        "This Month",
                        "${stats.totalDaysPresent} days",
                        MaterialTheme.colorScheme.primary,
                        Icons.Default.CalendarMonth
                    )
                }
                item {
                    UserStatCard(
                        "Streak",
                        "${stats.currentStreak} days",
                        MaterialTheme.colorScheme.error,
                        Icons.Default.LocalFireDepartment
                    )
                }
                item {
                    UserStatCard(
                        "Rate",
                        "${stats.monthlyAttendanceRate}%",
                        MaterialTheme.colorScheme.tertiary,
                        Icons.Default.TrendingUp
                    )
                }
                item {
                    UserStatCard(
                        "Records",
                        "${userAttendance.size}",
                        MaterialTheme.colorScheme.secondary,
                        Icons.Default.History
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // QR Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
        ) {

            Column(
                Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "My Attendance QR",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Tap to enlarge • Hold to share",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                )

                Card(
                    modifier = Modifier
                        .size(180.dp)
                        .combinedClickable(
                            onClick = { showFullScreenQr = true },
                            onLongClick = { viewModel.shareQrCode(context) }),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        qrBitmap?.let { bitmap ->
                            Image(bitmap.asImageBitmap(), "QR", Modifier.padding(12.dp))
                        } ?: CircularProgressIndicator()
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Recent Attendance
        Text("Recent Attendance", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        when {
            isLoading -> repeat(3) { AttendanceShimmerCard(); Spacer(Modifier.height(8.dp)) }
            userAttendance.isEmpty() -> EmptyAttendanceCard()
            else -> userAttendance.forEach {
                UserAttendanceCard(it)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun UserStatCard(title: String, value: String, color: Color, icon: ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(value, style = MaterialTheme.typography.titleMedium, color = color)
                Text(title, style = MaterialTheme.typography.bodySmall)
            }
            Icon(icon, null, tint = color)
        }
    }
}

@Composable
fun EmptyAttendanceCard() {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EventBusy,
                null,
                Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text("No records yet", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Your attendance will appear here",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AttendanceShimmerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Surface(
                    modifier = Modifier
                        .width(100.dp)
                        .height(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                ) {}
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier
                        .width(150.dp)
                        .height(12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {}
            }

            Surface(
                modifier = Modifier
                    .width(60.dp)
                    .height(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {}
        }
    }
}

@Composable
fun UserAttendanceCard(attendance: Attendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = attendance.date, style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Marked at: ${
                        formatTimeMillis(attendance.timeIn.toLong())
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = when (attendance.status) {
                    AttendanceStatus.PRESENT -> Color.Green.copy(alpha = 0.1f)
                    AttendanceStatus.LATE -> Color.Red.copy(alpha = 0.1f)
                    AttendanceStatus.ABSENT -> Color.Red.copy(alpha = 0.1f)
                }, shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = attendance.status.name,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = when (attendance.status) {
                        AttendanceStatus.PRESENT -> Color.Green
                        AttendanceStatus.LATE -> Color.Red
                        AttendanceStatus.ABSENT -> Color.Red
                    }
                )
            }
        }
    }
}


@Composable
fun FullScreenQrDialog(
    qrBitmap: Bitmap?,
    userName: String,
    userEmail: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(), color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss, colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // User info
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Large QR Code
                Card(
                    modifier = Modifier.size(300.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        qrBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code for $userName",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Show this QR code to admin for attendance marking",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                // Bottom buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White, contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }
                }
            }
        }
    }


}

