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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Soal
import com.example.data.database.StudentAttempt
import com.example.data.database.Ujian
import com.example.ui.viewmodel.CbtViewModel
import com.example.ui.theme.SlateBackground

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuruDashboard(
    viewModel: CbtViewModel,
    onLogout: () -> Unit
) {
    val teacherUser by viewModel.currentUser.collectAsState()
    val ujians by viewModel.allUjians.collectAsState()
    val attempts by viewModel.allAttempts.collectAsState()
    val aiStatus by viewModel.aiStatus.collectAsState()
    val aiGeneratedSoals by viewModel.generatedQuestions.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Screen tabs: "Daftar Ujian", "Buat Ujian & AI", "Monitoring Realtime", "Unduh Nilai"
    var selectedTab by remember { mutableStateOf(0) }

    // Forms for Ujian creation
    var examTitleInput by remember { mutableStateOf("") }
    var examTokenInput by remember { mutableStateOf("") }
    var examDurationInput by remember { mutableStateOf("45") }
    var examSubjectInput by remember { mutableStateOf("Matematika") }
    var examClassTargetInput by remember { mutableStateOf("XII-IPA-1") }

    // Manual/Excel text import parser
    var manualSoalsList = remember { mutableStateListOf<Soal>() }
    var showManualCreatorRow by remember { mutableStateOf(false) }

    // Draft individual manual question
    var draftType by remember { mutableStateOf("pilihan_ganda") }
    var draftQText by remember { mutableStateOf("") }
    var draftA by remember { mutableStateOf("") }
    var draftB by remember { mutableStateOf("") }
    var draftC by remember { mutableStateOf("") }
    var draftD by remember { mutableStateOf("") }
    var draftE by remember { mutableStateOf("") }
    var draftCorrectAns by remember { mutableStateOf("A") }
    var draftExplanation by remember { mutableStateOf("") }
    var draftDifficulty by remember { mutableStateOf("Sedang") }

    // AI question builder states
    var materialTxtInput by remember { mutableStateOf("") }
    var aiQCountInput by remember { mutableStateOf("3") }
    var aiQTypeInput by remember { mutableStateOf("pilihan_ganda") }
    var aiDifficultyInput by remember { mutableStateOf("Sedang") }

    // Clipboard simulated text import template
    var rawTextImportInput by remember { mutableStateOf("") }
    var showExcelImportDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // INTRO HEADER DECORATION
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Portal Pendidik SmartCBT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = teacherUser?.name ?: "Pak Pengajar, S.Pd.",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .testTag("teacher_logout_btn")
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "Siswa Logout", tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            // HORIZONTAL TABS CONTROLLER
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Kelola Ujian", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Buat Soal AI Builder", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Monitoring Realtime", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                    Text("Export Laporan Nilai", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
            }

            // CORE MULTI-SCREEN TABS DRAWAL
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 0: Exam active lists with active counts
                        Text(
                            text = "Daftar Ujian Aktif",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (ujians.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Belum ada ujian yang terdaftar. Gunakan tab AI Builder untuk membuat ujian instan!")
                            }
                        } else {
                            ujians.forEach { ujian ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Mapel: ${ujian.subject} • Target: ${ujian.classTarget}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            IconButton(
                                                onClick = { viewModel.deleteUjian(ujian) },
                                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus")
                                            }
                                        }
                                        Text(
                                            text = ujian.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Durasi: ${ujian.durationMinutes} menit", style = MaterialTheme.typography.bodySmall)
                                            Text("Token: ${ujian.code}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                            Text("Jumlah: ${ujian.questionCount} soal", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: AI Question Maker and Manual Forms Assembler
                        Text(
                            text = "Konfigurasi Paket Ujian Baru",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // General Exam Info Inputs
                        OutlinedTextField(
                            value = examTitleInput,
                            onValueChange = { examTitleInput = it },
                            label = { Text("Nama/Judul Ujian") },
                            placeholder = { Text("UAS Matematika Dasar") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = examTokenInput,
                                onValueChange = { examTokenInput = it.uppercase() },
                                label = { Text("Token Kode") },
                                placeholder = { Text("MAT26") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = examDurationInput,
                                onValueChange = { examDurationInput = it },
                                label = { Text("Durasi (Menit)") },
                                placeholder = { Text("60") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp)) {
                            OutlinedTextField(
                                value = examSubjectInput,
                                onValueChange = { examSubjectInput = it },
                                label = { Text("Mata Pelajaran") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = examClassTargetInput,
                                onValueChange = { examClassTargetInput = it },
                                label = { Text("Target Kelas") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // THE METEORIC FEATURE: GEMINI AI GENERATIVE BUILDER SECTION
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Asisten Pembuat Soal AI (Gemini Power)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                                Text(
                                    text = "Tempelkan inti/rangkuman materi, AI secara otomatis merumuskan soal bermateri komprehensif lengkap dengan kunci pilihan dan pembahasan jawaban.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )

                                OutlinedTextField(
                                    value = materialTxtInput,
                                    onValueChange = { materialTxtInput = it },
                                    label = { Text("Ketik atau Tempel Kunci Materi Pelajaran") },
                                    placeholder = { Text("Contoh: Hukum Newton I menyatakan bahwa benda diam akan tetap diam dan benda bergerak lurus beraturan akan tetap..." ) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .testTag("teacher_material_input"),
                                    maxLines = 8,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // AI Options Type Selector
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = aiQTypeInput == "pilihan_ganda", onClick = { aiQTypeInput = "pilihan_ganda" })
                                        Text("Pilihan Ganda", style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        RadioButton(selected = aiQTypeInput == "essay", onClick = { aiQTypeInput = "essay" })
                                        Text("Esai / Essay", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Difficulties Selector
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Kesulitan: ", style = MaterialTheme.typography.labelSmall)
                                        listOf("Mudah", "Sedang", "Sulit").forEach { diff ->
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 4.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (aiDifficultyInput == diff) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                    .clickable { aiDifficultyInput = diff }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = diff,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (aiDifficultyInput == diff) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = aiQCountInput,
                                        onValueChange = { aiQCountInput = it },
                                        label = { Text("Jumlah") },
                                        modifier = Modifier.width(80.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (materialTxtInput.trim().isEmpty()) {
                                            Toast.makeText(context, "Sertakan materi rujukan terlebih dahulu.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.generateAIQuestions(
                                                materialText = materialTxtInput,
                                                questionType = aiQTypeInput,
                                                difficulty = aiDifficultyInput,
                                                count = aiQCountInput.toIntOrNull() ?: 3
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("teacher_ai_generate_btn"),
                                    enabled = aiStatus != "LOADING"
                                ) {
                                    if (aiStatus == "LOADING") {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Menganalisa & Merumuskan...")
                                    } else {
                                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Buat Soal Menggunakan AI Now", fontWeight = FontWeight.Bold)
                                    }
                                }

                                // If API key missing display clean banner
                                if (aiStatus == "API_KEY_MISSING") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "⚠️ API Key Gemini belum dikonfigurasi. Harap masukkan API Key Gemini Anda di panel Secrets AI Studio agar generator berfungsi.",
                                        color = Color(0xFFDC2626),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // PREVIEW GENERATED AI LIST
                        if (aiGeneratedSoals.isNotEmpty()) {
                            Text(
                                text = "Hasil AI Draft Soal (${aiGeneratedSoals.size} Soal Draft)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF10B981)),
                                modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
                            )

                            aiGeneratedSoals.forEachIndexed { index, soal ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Draft Soal #${index+1} • (${soal.difficulty})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                        Text(soal.questionText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        if (soal.type == "pilihan_ganda") {
                                            Text("A. ${soal.optionA}", style = MaterialTheme.typography.bodySmall)
                                            Text("B. ${soal.optionB}", style = MaterialTheme.typography.bodySmall)
                                            Text("C. ${soal.optionC}", style = MaterialTheme.typography.bodySmall)
                                            Text("D. ${soal.optionD}", style = MaterialTheme.typography.bodySmall)
                                            Text("E. ${soal.optionE}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text("Kunci Jawaban: ${soal.correctAnswer}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                        Text("Pembahasan: ${soal.explanation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    }
                                }
                            }

                            Row {
                                Button(
                                    onClick = {
                                        if (examTitleInput.trim().isEmpty() || examTokenInput.trim().isEmpty()) {
                                            Toast.makeText(context, "Nama ujian dan token harus diisi di konfigurasi atas terlebih dahulu.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.addNewUjian(
                                                title = examTitleInput,
                                                code = examTokenInput,
                                                durationMin = examDurationInput.toIntOrNull() ?: 45,
                                                subject = examSubjectInput,
                                                classTarget = examClassTargetInput,
                                                soals = aiGeneratedSoals,
                                                onCompleted = {
                                                    Toast.makeText(context, "Ujian berhasil dibentuk dari AI!", Toast.LENGTH_SHORT).show()
                                                    examTitleInput = ""
                                                    examTokenInput = ""
                                                    viewModel.clearGeneratedAiQuestions()
                                                    selectedTab = 0
                                                }
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Text("Simpan & Luncurkan Ujian", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.clearGeneratedAiQuestions() },
                                    modifier = Modifier.weight(0.8f)
                                ) {
                                    Text("Buang Draft")
                                }
                            }
                        }

                        // MANUAL EXCEL COPY PASTE PORTAL SIMULATION
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Daftar Soal Manual (${manualSoalsList.size})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Row {
                                TextButton(onClick = { showExcelImportDialog = true }) {
                                    Icon(Icons.Default.UploadFile, null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simulasi Excel CSV")
                                }
                                TextButton(onClick = { showManualCreatorRow = !showManualCreatorRow }) {
                                    Icon(if (showManualCreatorRow) Icons.Default.Close else Icons.Default.Add, null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tambah")
                                }
                            }
                        }

                        // Manual Draft Creator form expand
                        AnimatedVisibility(visible = showManualCreatorRow) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Definisikan Pertanyaan Manual", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                        RadioButton(selected = draftType == "pilihan_ganda", onClick = { draftType = "pilihan_ganda" })
                                        Text("Pilihan Ganda", style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        RadioButton(selected = draftType == "essay", onClick = { draftType = "essay" })
                                        Text("Esai", style = MaterialTheme.typography.bodySmall)
                                    }

                                    OutlinedTextField(
                                        value = draftQText,
                                        onValueChange = { draftQText = it },
                                        label = { Text("Isi Teks Soal/Pertanyaan") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (draftType == "pilihan_ganda") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(value = draftA, onValueChange = { draftA = it }, label = { Text("Opsi A") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = draftB, onValueChange = { draftB = it }, label = { Text("Opsi B") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = draftC, onValueChange = { draftC = it }, label = { Text("Opsi C") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = draftD, onValueChange = { draftD = it }, label = { Text("Opsi D") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = draftE, onValueChange = { draftE = it }, label = { Text("Opsi E") }, modifier = Modifier.fillMaxWidth())
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(value = draftCorrectAns, onValueChange = { draftCorrectAns = it.uppercase() }, label = { Text("Kunci Jawaban Benar (A/B/C/D/E)") }, modifier = Modifier.width(180.dp))
                                    } else {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(value = draftCorrectAns, onValueChange = { draftCorrectAns = it }, label = { Text("Referensi Jawaban Esai Benar") }, modifier = Modifier.fillMaxWidth())
                                    }

                                    OutlinedTextField(value = draftExplanation, onValueChange = { draftExplanation = it }, label = { Text("Pembahasan / Kunci Solusi") }, modifier = Modifier.fillMaxWidth())

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            if (draftQText.trim().isEmpty()) {
                                                Toast.makeText(context, "Pertanyaan masih kosong.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val q = Soal(
                                                    examId = 0,
                                                    questionText = draftQText,
                                                    type = draftType,
                                                    optionA = draftA,
                                                    optionB = draftB,
                                                    optionC = draftC,
                                                    optionD = draftD,
                                                    optionE = draftE,
                                                    correctAnswer = draftCorrectAns,
                                                    explanation = draftExplanation,
                                                    difficulty = draftDifficulty
                                                )
                                                manualSoalsList.add(q)
                                                draftQText = ""
                                                draftA = ""
                                                draftB = ""
                                                draftC = ""
                                                draftD = ""
                                                draftE = ""
                                                draftCorrectAns = "A"
                                                draftExplanation = ""
                                                showManualCreatorRow = false
                                                Toast.makeText(context, "Soal manual terdraft!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Kunci Soal Ke Draft List")
                                    }
                                }
                            }
                        }

                        // Manual questions list items
                        manualSoalsList.forEachIndexed { index, q ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${index+1}. ", fontWeight = FontWeight.Bold)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(q.questionText, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                        Text("Type: ${q.type} • Key: ${q.correctAnswer}", style = MaterialTheme.typography.labelSmall)
                                    }
                                    IconButton(onClick = { manualSoalsList.removeAt(index) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // Save whole Manual exam structure
                        if (manualSoalsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (examTitleInput.trim().isEmpty() || examTokenInput.trim().isEmpty()) {
                                        Toast.makeText(context, "Sertakan Judul Ujian dan Token di bagian atas terlebih dahulu.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.addNewUjian(
                                            title = examTitleInput,
                                            code = examTokenInput,
                                            durationMin = examDurationInput.toIntOrNull() ?: 45,
                                            subject = examSubjectInput,
                                            classTarget = examClassTargetInput,
                                            soals = manualSoalsList.toList(),
                                            onCompleted = {
                                                Toast.makeText(context, "Ujian manual berhasil diluncurkan!", Toast.LENGTH_SHORT).show()
                                                examTitleInput = ""
                                                examTokenInput = ""
                                                manualSoalsList.clear()
                                                selectedTab = 0
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Luncurkan Paket Ujian Manual ini", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: Realtime Student Progress tracking telemetry logs
                        Text(
                            text = "Monitoring Progress Siswa Aktif",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (attempts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Belum ada aktvitas pengerjaan ujian tercatat di database.")
                            }
                        } else {
                            attempts.forEach { att ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (att.warningsCount > 2) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.dp, if (att.warningsCount > 2) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = att.studentName,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = if (att.isCompleted) "Status: Selesai" else "Mengerjakan",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (att.isCompleted) Color(0xFF10B981) else Color(0xFFF59E0B)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Ujian: ${att.examTitle} • NIS: ${att.studentNis}", style = MaterialTheme.typography.bodySmall)
                                        Text("Skor: ${att.score} • Peringatan Curang: ${att.warningsCount} kali", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                                        if (att.warningsCount > 0) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("LOG: Deteksi keluar window aplikasi!", style = MaterialTheme.typography.labelSmall.copy(color = Color.Red, fontWeight = FontWeight.Bold))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: Export grades list panel with Download click feedback
                        Text(
                            text = "Rekapitulasi Nilai & Laporan Ekspor",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (attempts.filter { it.isCompleted }.isEmpty()) {
                            Text("Belum ada nilai ujian terkumpul untuk direkap.")
                        } else {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                attempts.filter { it.isCompleted }.forEach { att ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(att.studentName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                            Text("Mapel: ${att.examTitle}", style = MaterialTheme.typography.labelSmall)
                                        }
                                        Text(
                                            text = "Skor: ${att.score}",
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "📥 Mengekspor data ke Excel & PDF... Download Berhasil!", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.FileDownload, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Unduh Rekap Spreadsheet (Excel/PDF)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // CSV SIMULATION COMPANION MODAL
        if (showExcelImportDialog) {
            AlertDialog(
                onDismissRequest = { showExcelImportDialog = false },
                title = { Text("Simulasi Unggah Soal via Excel CSV") },
                text = {
                    Column {
                        Text("Tempel teks representasi baris spreadsheet (format: Pertanyaan | Tipe | Opsi A | Opsi B | Kunci) di bawah ini.", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = rawTextImportInput,
                            onValueChange = { rawTextImportInput = it },
                            placeholder = { Text("Sebutkan rumus Newton II? | pilihan_ganda | F=m.a | E=m.c2 | A") },
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val row = rawTextImportInput.trim()
                            if (row.isNotEmpty()) {
                                val parts = row.split("|").map { it.trim() }
                                if (parts.size >= 5) {
                                    val q = Soal(
                                        examId = 0,
                                        questionText = parts[0],
                                        type = parts[1],
                                        optionA = parts[2],
                                        optionB = parts[3],
                                        optionC = if(parts.size >= 6) parts[4] else "",
                                        correctAnswer = parts.last()
                                    )
                                    manualSoalsList.add(q)
                                    Toast.makeText(context, "Baris CSV berhasil diparse!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Format baris CSV kurang lengkap.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            rawTextImportInput = ""
                            showExcelImportDialog = false
                        }
                    ) {
                        Text("Mulai Parse")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExcelImportDialog = false }) {
                        Text("Kembali")
                    }
                }
            )
        }
    }
}
