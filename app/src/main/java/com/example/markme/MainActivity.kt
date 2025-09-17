package com.example.markme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.markme.data.local.SessionManager
import com.example.markme.domain.model.UserRole
import com.example.markme.presentation.navigation.MarkMeNavigation
import com.example.markme.presentation.navigation.Screens
import com.example.markme.ui.theme.MarkMeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val role = sessionManager.getRole()
        val startScreen: Screens = when (role) {
            UserRole.ADMIN -> Screens.AdminDashboardScreen
            UserRole.STUDENT -> Screens.UserDashboardScreen
            null -> Screens.SignInScreen
        }

        setContent {
            MarkMeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MarkMeNavigation(paddingValues = innerPadding, startDestination = startScreen)
                }
            }
        }
    }
}
