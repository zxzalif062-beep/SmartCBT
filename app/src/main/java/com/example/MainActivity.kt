package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CbtViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CbtViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CbtAppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun CbtAppNavigation(viewModel: CbtViewModel) {
    var currentRoute by remember { mutableStateOf("landing") }
    var registerMode by remember { mutableStateOf(false) }

    when (currentRoute) {
        "landing" -> {
            LandingPage(
                onNavigateToLogin = {
                    registerMode = false
                    currentRoute = "login"
                },
                onNavigateToRegister = {
                    registerMode = true
                    currentRoute = "login"
                }
            )
        }
        "login" -> {
            LoginPage(
                viewModel = viewModel,
                isRegisterMode = registerMode,
                onAuthSuccess = { role ->
                    when (role) {
                        "siswa" -> currentRoute = "siswa_dashboard"
                        "guru" -> currentRoute = "guru_dashboard"
                        "admin" -> currentRoute = "admin_dashboard"
                    }
                },
                onTabSwitch = {
                    registerMode = !registerMode
                }
            )
        }
        "siswa_dashboard" -> {
            SiswaDashboard(
                viewModel = viewModel,
                onNavigateToUjian = {
                    currentRoute = "ujian"
                },
                onNavigateToHasilUjian = {
                    currentRoute = "hasil_ujian"
                },
                onLogout = {
                    viewModel.logout()
                    currentRoute = "landing"
                }
            )
        }
        "ujian" -> {
            UjianScreen(
                viewModel = viewModel,
                onNavigateBackToDashboard = {
                    currentRoute = "siswa_dashboard"
                },
                onSubmitComplete = {
                    currentRoute = "hasil_ujian"
                }
            )
        }
        "hasil_ujian" -> {
            HasilUjianScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    currentRoute = "siswa_dashboard"
                }
            )
        }
        "guru_dashboard" -> {
            GuruDashboard(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    currentRoute = "landing"
                }
            )
        }
        "admin_dashboard" -> {
            AdminDashboard(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    currentRoute = "landing"
                }
            )
        }
    }
}
