package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.User
import com.example.ui.viewmodel.CbtViewModel
import com.example.ui.theme.SlateBackground

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDashboard(
    viewModel: CbtViewModel,
    onLogout: () -> Unit
) {
    val adminUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allUjians by viewModel.allUjians.collectAsState()
    val allAttempts by viewModel.allAttempts.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Internal navigation tabs: "Kelola Pengguna", "Kelola Akademik", "Statistik & Sistem"
    var selectedSection by remember { mutableStateOf(0) }

    // Forms for creating accounts
    var newId by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newRole by remember { mutableStateOf("siswa") } // "siswa", "guru", "admin"
    var newClass by remember { mutableStateOf("XII-IPA-1") }

    var showAddUserRow by remember { mutableStateOf(false) }

    // Anti cheat strictness settings
    var cheatLimitSetting by remember { mutableStateOf(3) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // ADMIN INTRO TOP NAV HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Portal Administrasi SMARTCBT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = adminUser?.name ?: "Kepala Admin",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .testTag("admin_logout_btn")
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "Log out", tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            // SEGMENT BAR BUTTONS CONTROLLER
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(selected = selectedSection == 0, onClick = { selectedSection = 0 }) {
                    Text("Kelola Akun", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedSection == 1, onClick = { selectedSection = 1 }) {
                    Text("Kelola Kelas & Mapel", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedSection == 2, onClick = { selectedSection = 2 }) {
                    Text("Statistik & Konfigurasi", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
            }

            // CORE SCREEN
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                when (selectedSection) {
                    0 -> {
                        // KELOLA AKUN USERS LISTS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Daftar Pengguna Aktif (${allUsers.size} Users)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Button(
                                onClick = { showAddUserRow = !showAddUserRow },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = if (showAddUserRow) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Akun Baru")
                            }
                        }

                        // Form Create Accounts
                        AnimatedVisibility(visible = showAddUserRow) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Tambah / Modifikasi Akun", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = newId,
                                        onValueChange = { newId = it },
                                        label = { Text("Username (NIS / Email)") },
                                        placeholder = { Text("65432 atau admin2@cbt.com") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    OutlinedTextField(
                                        value = newName,
                                        onValueChange = { newName = it },
                                        label = { Text("Nama Lengkap") },
                                        placeholder = { Text("Eka Pratama, M.Si.") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    OutlinedTextField(
                                        value = newPassword,
                                        onValueChange = { newPassword = it },
                                        label = { Text("Kata Sandi") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    // Role Radio Row selectors
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text("Role: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        listOf("siswa", "guru", "admin").forEach { role ->
                                            RadioButton(selected = newRole == role, onClick = { newRole = role })
                                            Text(role.uppercase(), style = MaterialTheme.typography.bodySmall)
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                    }

                                    if (newRole == "siswa") {
                                        OutlinedTextField(
                                            value = newClass,
                                            onValueChange = { newClass = it },
                                            label = { Text("Kelas Siswa") },
                                            placeholder = { Text("XII-IPS-2") },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (newId.trim().isEmpty() || newPassword.trim().isEmpty()) {
                                                Toast.makeText(context, "ID dan password tidak boleh kosong.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                viewModel.addOrUpdateUserAdmin(
                                                    idInput = newId,
                                                    nameInput = if (newName.trim().isEmpty()) "User Baru" else newName,
                                                    pass = newPassword,
                                                    role = newRole,
                                                    className = newClass
                                                )
                                                newId = ""
                                                newName = ""
                                                newPassword = ""
                                                showAddUserRow = false
                                                Toast.makeText(context, "Operasi akun berhasil!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Eksekusi & Simpan Akun")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render user lists
                        allUsers.forEach { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when(user.role) {
                                                    "admin" -> Color(0xFFEF4444).copy(alpha = 0.1f)
                                                    "guru" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                    else -> Color(0xFF10B981).copy(alpha = 0.1f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when(user.role) {
                                                "admin" -> Icons.Default.AdminPanelSettings
                                                "guru" -> Icons.Default.School
                                                else -> Icons.Default.Person
                                            },
                                            contentDescription = null,
                                            tint = when(user.role) {
                                                "admin" -> Color(0xFFEF4444)
                                                "guru" -> MaterialTheme.colorScheme.primary
                                                else -> Color(0xFF10B981)
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("ID: ${user.id} • Role: ${user.role.uppercase()} ${if(user.className.isNotEmpty()) "• "+user.className else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                    }

                                    // Prevent self deleting
                                    if (user.id != adminUser?.id) {
                                        IconButton(onClick = { viewModel.deleteUserAdmin(user) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // KELOLA AKADEMIK (Kelas & Mapel)
                        Text("Kelola Kurikulum & Target Akademik", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))

                        val mockClasses = listOf("XII-IPA-1", "XII-IPA-2", "XII-IPS-1", "XII-IPS-2")
                        val mockMapel = listOf("Matematika", "Bahasa Indonesia", "Fisika", "Biologi", "Kimia", "Sejarah")

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Daftar Kelas Aktif Kurikulum", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow {
                                    mockClasses.forEach { cls ->
                                        Box(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(cls, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Daftar Mata Pelajaran SmartCBT", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow {
                                    mockMapel.forEach { mapel ->
                                        Box(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(mapel, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // STATISTIK SYSTEM CARD & CONFIGS
                        Text("Statistik Penggunaan Aplikasi", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Metrics cards row
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Card(
                                modifier = Modifier.weight(1f).padding(end = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Ujian Dirilis", style = MaterialTheme.typography.labelSmall)
                                    Text("${allUjians.size} Paket", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f).padding(start = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Ujian Selesai", style = MaterialTheme.typography.labelSmall)
                                    Text("${allAttempts.size} Lembar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // System state analytics
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Integritas & Keamanan Sistem", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Database Engine: PostgreSQL/SQLite", style = MaterialTheme.typography.bodySmall)
                                    Text("Status: OK", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Konektivitas Cloud: Firebase", style = MaterialTheme.typography.bodySmall)
                                    Text("Terhubung", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // CONFIGURATIONS CONTROLS CARD
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pengaturan Server & Anti-Cheat", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Batas Peringatan Toleransi Curang (Out of window): $cheatLimitSetting kali", style = MaterialTheme.typography.bodySmall)
                                Slider(
                                    value = cheatLimitSetting.toFloat(),
                                    onValueChange = { cheatLimitSetting = it.toInt() },
                                    valueRange = 1f..5f,
                                    steps = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Paksa Mode Layar Penuh (Fullscreen)", style = MaterialTheme.typography.bodySmall)
                                    Switch(checked = true, onCheckedChange = {})
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // BACKUP DATABASE SIMULATOR CTA
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "💾 Database SmartCBT berhasil dibackup ke cloud server sekolah!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Backup, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cadangkan Database Sistem (Cloud Backup)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
