package com.example.markme.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Screens : NavKey {

    @Serializable
    data object SignInScreen : Screens

    @Serializable
    data object SignUpScreen : Screens

    @Serializable
    data object AdminDashboardScreen : Screens

    @Serializable
    data object ScannerScreen : Screens
    @Serializable
    data object AttendanceListScreen : Screens

    @Serializable
    data object UserManagementScreen : Screens

    @Serializable
    data object UserDashboardScreen : Screens

    @Serializable
    data object AnalyticsScreen : Screens
}