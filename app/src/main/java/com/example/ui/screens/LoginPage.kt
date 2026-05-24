package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CbtViewModel
import com.example.ui.theme.*

@Composable
fun LoginPage(
    viewModel: CbtViewModel,
    isRegisterMode: Boolean = false,
    onAuthSuccess: (String) -> Unit, // Callback passing the role "siswa", "guru", "admin"
    onTabSwitch: () -> Unit // Changes register view mode
) {
    var userId by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("siswa") } // "siswa", "guru", "admin"
    var className by remember { mutableStateOf("XII-IPA-1") } // for siswa
    var showPassword by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val scrollState = rememberScrollState()

    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotIdInput by remember { mutableStateOf("") }
    var forgotResult by remember { mutableStateOf("") }

    val isDarkMode = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))
 
            // High Density Modern Header Branding Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (isDarkMode) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF2563EB), Color(0xFF6366F1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isRegisterMode) "Pendaftaran SmartCBT" else "SmartCBT Platform",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isRegisterMode) "Buat akun baru untuk memulai evaluasi belajar" else "Kredensial Resmi Evaluasi Sekolah Terintegrasi",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // High Density Inputs Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (isDarkMode) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Role Switch Row
                    Text(
                        text = "Masuk Sebagai",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            .border(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("siswa", "guru", "admin").forEach { role ->
                            val isSelected = selectedRole == role
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedRole = role
                                        // Preset default placeholders to help users
                                        if (role == "siswa") {
                                            userId = "12345"
                                            password = "siswa"
                                        } else if (role == "guru") {
                                            userId = "guru@cbt.com"
                                            password = "guru"
                                        } else {
                                            userId = "admin@cbt.com"
                                            password = "admin"
                                        }
                                    }
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = role.lowercase().replaceFirstChar { it.uppercase() },
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Error Display with Nice Alert Card
                    AnimatedVisibility(visible = authError != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error icon",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Username or NIS text field
                    OutlinedTextField(
                        value = userId,
                        onValueChange = { userId = it },
                        label = { Text(if (selectedRole == "siswa") "NIS Siswa" else "Alamat Email") },
                        placeholder = { Text(if (selectedRole == "siswa") "12345" else "guru@cbt.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("auth_username_input"),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = if (selectedRole == "siswa") Icons.Default.Badge else Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDarkMode) CardDarkBG else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDarkMode) CardDarkBG else Color(0xFFF8FAFC),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isDarkMode) Color.Transparent else Color(0xFFE2E8F0)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (selectedRole == "siswa") KeyboardType.Number else KeyboardType.Email
                        )
                    )

                    // Register Name text field
                    AnimatedVisibility(visible = isRegisterMode) {
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("Nama Lengkap") },
                            placeholder = { Text("Masukkan nama lengkap Anda") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("auth_fullname_input"),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDarkMode) CardDarkBG else Color(0xFFF8FAFC),
                                unfocusedContainerColor = if (isDarkMode) CardDarkBG else Color(0xFFF8FAFC),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDarkMode) Color.Transparent else Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // Class Selection for student registration
                    AnimatedVisibility(visible = isRegisterMode && selectedRole == "siswa") {
                        OutlinedTextField(
                            value = className,
                            onValueChange = { className = it },
                            label = { Text("Kelas (cth: XII-IPA-1)") },
                            placeholder = { Text("XII-IPA-1") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("auth_class_input"),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDarkMode) CardDarkBG else Color(0xFFF8FAFC),
                                unfocusedContainerColor = if (isDarkMode) CardDarkBG else Color(0xFFF8FAFC),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDarkMode) Color.Transparent else Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Kata Sandi") },
                        placeholder = { Text("••••••••") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("auth_password_input"),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            val icon = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(imageVector = icon, contentDescription = "Toggle password visibility", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDarkMode) CardDarkBG else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDarkMode) CardDarkBG else Color(0xFFF8FAFC),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isDarkMode) Color.Transparent else Color(0xFFE2E8F0)
                        ),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    // Extra Help Info for quick login simulation
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Tips",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Simulasi Demo: Ketuk tombol role di atas untuk login cepat.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Forget password handler
                    if (!isRegisterMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Lupa kata sandi?",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .clickable {
                                        forgotIdInput = userId
                                        forgotResult = ""
                                        showForgotDialog = true
                                    }
                                    .testTag("auth_forgot_password")
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PRIMARY BUTTON
            Button(
                onClick = {
                    if (isRegisterMode) {
                        viewModel.register(
                            idInput = userId,
                            nameInput = if (userName.trim().isEmpty()) "Member Baru" else userName,
                            passwordInput = password,
                            role = selectedRole,
                            className = className,
                            onSuccess = { onAuthSuccess(selectedRole) }
                        )
                    } else {
                        viewModel.login(
                            idInput = userId,
                            passwordInput = password,
                            onLogged = onAuthSuccess
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isRegisterMode) "Daftar Sekarang" else "Masuk Sekarang",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub text toggle modes
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isRegisterMode) "Sudah mempunyai akun? " else "Belum mempunyai akun? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = if (isRegisterMode) "Masuk" else "Daftar di sini",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .clickable { onTabSwitch() }
                        .testTag("auth_toggle_mode_link")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Forgot password modal simulation
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Bantuan Lupa Kata Sandi") },
            text = {
                Column {
                    Text(
                        text = "Ketikkan NIS siswa atau alamat email guru Anda untuk memulihkan akun.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = forgotIdInput,
                        onValueChange = { forgotIdInput = it },
                        placeholder = { Text("ID / NIS / Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (forgotResult.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = forgotResult,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetPassword(forgotIdInput) { result ->
                            forgotResult = result
                        }
                    }
                ) {
                    Text("Temukan Sandi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}
