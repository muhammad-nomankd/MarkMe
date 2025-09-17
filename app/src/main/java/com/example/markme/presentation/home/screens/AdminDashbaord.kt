package com.example.markme.presentation.home.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.markme.data.local.Attendance
import com.example.markme.presentation.authentication.viewmodel.AuthViewModel
import com.example.markme.presentation.home.viewmodel.DashboardViewModel
import com.example.markme.presentation.home.viewmodel.UserDashboardViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    DelicateCoroutinesApi::class
)
@Composable
fun AdminDashboard(
    onLogout: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    paddingValues: PaddingValues,
    userDashboardViewModel: UserDashboardViewModel = hiltViewModel()
) {
    val dashboardStats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by userDashboardViewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(Date()) }
    val scope = rememberCoroutineScope()
    val attendanceList by viewModel.attendanceList.collectAsStateWithLifecycle()

    // Update current time every minute
    LaunchedEffect(Unit) {
        viewModel.loadDashboardStats()
        while (true) {
            currentTime = Date()
            kotlinx.coroutines.delay(60000)
        }
    }

    val handleRefresh = {
        isRefreshing = true
        viewModel.loadDashboardStats()
        // Simulate refresh delay
        scope.launch {
            kotlinx.coroutines.delay(1000)
            isRefreshing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Enhanced Top App Bar
        TopAppBar(
            title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${currentUser?.fullName}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Attendance Management",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }, actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = SimpleDateFormat(
                                "MMM dd, hh:m:a", Locale.getDefault()
                            ).format(currentTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Refresh button with animation
                IconButton(
                    onClick = { handleRefresh() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (isRefreshing) {
                            Modifier.rotate(360f)
                        } else Modifier
                    )
                }

                // Logout button
                IconButton(
                    onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "📊 Today's Overview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            AnimatedContent(
                targetState = dashboardStats, transitionSpec = {
                    slideInVertically { it } + fadeIn() with slideOutVertically { -it } + fadeOut()
                }) { stats ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    item {
                        EnhancedStatCard(
                            title = "Total Users",
                            value = stats.totalUsers.toString(),
                            icon = Icons.Default.Group,
                            gradient = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF6366F1), Color(0xFF8B5CF6)
                                )
                            ),
                            trend = "+12%"
                        )
                    }
                    item {
                        EnhancedStatCard(
                            title = "Present Today",
                            value = stats.presentToday.toString(),
                            icon = Icons.Default.CheckCircle,
                            gradient = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF10B981), Color(0xFF059669)
                                )
                            ),
                            trend = "+5%"
                        )
                    }
                    item {
                        EnhancedStatCard(
                            title = "Absent Today",
                            value = stats.absentToday.toString(),
                            icon = Icons.Default.Cancel,
                            gradient = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFEF4444), Color(0xFFDC2626)
                                )
                            ),
                            trend = "-8%"
                        )
                    }
                    item {
                        EnhancedStatCard(
                            title = "Attendance Rate",
                            value = "${stats.attendanceRate}%",
                            icon = Icons.Default.TrendingUp,
                            gradient = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFF59E0B), Color(0xFFD97706)
                                )
                            ),
                            trend = "+2%"
                        )
                    }
                }
            }

            // Quick Actions Section
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(240.dp)
            ) {
                item {
                    EnhancedActionButton(
                        title = "Scan QR Code",
                        subtitle = "Mark attendance",
                        icon = Icons.Default.QrCodeScanner,
                        gradient = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF3B82F6), Color(0xFF1D4ED8)
                            )
                        ),
                        onClick = onNavigateToScanner
                    )
                }
                item {
                    EnhancedActionButton(
                        title = "View Attendance",
                        subtitle = "Check records",
                        icon = Icons.Default.Assignment,
                        gradient = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF10B981), Color(0xFF047857)
                            )
                        ),
                        onClick = onNavigateToAttendance
                    )
                }
                item {
                    EnhancedActionButton(
                        title = "Manage Users",
                        subtitle = "User settings",
                        icon = Icons.Default.ManageAccounts,
                        gradient = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6), Color(0xFF7C3AED)
                            )
                        ),
                        onClick = onNavigateToUsers
                    )
                }
                item {
                    EnhancedActionButton(
                        title = "Analytics",
                        subtitle = "View insights",
                        icon = Icons.Default.Analytics,
                        gradient = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFF59E0B), Color(0xFFD97706)
                            )
                        ),
                        onClick = onNavigateToAnalytics)
                }
            }

            // Recent Activity Section
            RecentActivitySection(attendanceList)
        }
    }
}

@Composable
private fun EnhancedStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: Brush,
    trend: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp), color = if (trend.startsWith("+")) {
                            Color(0xFF10B981).copy(alpha = 0.2f)
                        } else {
                            Color(0xFFEF4444).copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = trend,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (trend.startsWith("+")) {
                                Color(0xFF10B981)
                            } else {
                                Color(0xFFEF4444)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSystemInDarkTheme: Boolean = isSystemInDarkTheme()
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable(
                indication = null, interactionSource = remember { MutableInteractionSource() }) {
                isPressed = true
                onClick()
            }, elevation = CardDefaults.cardElevation(
        defaultElevation = if (isPressed) 8.dp else 4.dp
    ), colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Gradient overlay
            Box(
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSystemInDarkTheme) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun RecentActivitySection(attendanceList: List<Attendance>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (attendanceList.isEmpty()) {
                    RecentActivityItem(
                        title = "No recent activity",
                        subtitle = "No attendance records found",
                        icon = Icons.Default.Info,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    attendanceList.forEach {
                        RecentActivityItem(
                            title = it.userName,
                            subtitle = formatTimeMillis(it.timeIn.toLong()),
                            icon = Icons.Default.CheckCircle,
                            iconColor = Color(0xFF10B981)
                        )
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentActivityItem(
    title: String, subtitle: String, icon: ImageVector, iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            color = iconColor.copy(alpha = 0.2f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}