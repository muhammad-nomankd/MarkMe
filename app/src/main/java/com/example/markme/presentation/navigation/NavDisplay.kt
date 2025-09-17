package com.example.markme.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.example.markme.presentation.authentication.screens.SignInScreen
import com.example.markme.presentation.authentication.screens.SignUpScreen
import com.example.markme.presentation.authentication.viewmodel.AuthViewModel
import com.example.markme.presentation.home.screens.AdminDashboard
import com.example.markme.presentation.home.screens.AttendanceListScreen
import com.example.markme.presentation.home.screens.QrScannerScreen
import com.example.markme.presentation.home.screens.UserDashboard
import com.example.markme.presentation.home.screens.UserManagementScreen
import com.example.markme.presentation.home.screens.AnalyticsScreen
import com.example.markme.presentation.home.viewmodel.DashboardViewModel
import com.example.markme.presentation.home.viewmodel.UserDashboardViewModel

@Composable
fun MarkMeNavigation(paddingValues: PaddingValues, startDestination: Screens) {

    val backStack = rememberNavBackStack(startDestination)

    val adminDashBoardViewModel: DashboardViewModel = hiltViewModel()
    val userDashBoardViewModel: UserDashboardViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser = userDashBoardViewModel.currentUser.collectAsState().value

    NavDisplay(
        backStack = backStack, onBack = { count ->
            repeat(count) { backStack.removeLastOrNull() }

        }, entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ), entryProvider = entryProvider {
            entry<Screens.SignInScreen> {
                SignInScreen(
                    paddingValues = paddingValues,
                    onNavigateToSignUp = { backStack.add(Screens.SignUpScreen) },
                    onNavigateToAdminHome = { backStack.add(Screens.AdminDashboardScreen) },
                    onNavigateToStudentHome = { backStack.add(Screens.UserDashboardScreen) })
            }
            entry(Screens.SignUpScreen) {
                SignUpScreen(
                    paddingValues = paddingValues,
                    onNavigateBackToSignIn = { backStack.removeLastOrNull() },
                    onNavigateToAdminHome = { backStack.add(Screens.AdminDashboardScreen) },
                    onNavigateToStudentHome = { backStack.add(Screens.UserDashboardScreen) })
            }
            entry(Screens.AdminDashboardScreen) {
                AdminDashboard(
                    viewModel = adminDashBoardViewModel,
                    onLogout = { backStack.add(Screens.SignInScreen) },
                    onNavigateToScanner = { backStack.add(Screens.ScannerScreen) },
                    onNavigateToAttendance = { backStack.add(Screens.AttendanceListScreen) },
                    onNavigateToUsers = { backStack.add(Screens.UserManagementScreen) },
                    onNavigateToAnalytics = { backStack.add(Screens.AnalyticsScreen) },
                    paddingValues = paddingValues
                )
            }

            entry(Screens.ScannerScreen) {
                QrScannerScreen(
                    onBackPressed = {
                        backStack.removeLastOrNull()
                    }, paddingValues = paddingValues
                )
            }

            entry(Screens.AttendanceListScreen) {
                AttendanceListScreen(onBackPressed = { backStack.removeLastOrNull() },paddingValues)
            }

            entry(Screens.UserManagementScreen) {
                UserManagementScreen(
                    onBackPressed = {
                    backStack.removeLastOrNull()
                }, paddingValues)
            }
            entry(Screens.UserDashboardScreen) {
                UserDashboard(
                    currentUser = currentUser, onLogout = {
                        authViewModel.logout()
                        backStack.removeLastOrNull()
                        backStack.add(Screens.SignInScreen)
                    }, paddingValues = paddingValues
                )
            }
            entry(Screens.AnalyticsScreen) {
                AnalyticsScreen(onBackPressed = { backStack.removeLastOrNull() }, paddingValues = paddingValues)
            }
        })
}