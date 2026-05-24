package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.database.Soal
import com.example.ui.viewmodel.CbtViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UjianScreen(
    viewModel: CbtViewModel,
    onNavigateBackToDashboard: () -> Unit,
    onSubmitComplete: () -> Unit
) {
    val activeUjian by viewModel.activeUjian.collectAsState()
    val questions by viewModel.activeQuestions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val answers by viewModel.answersState.collectAsState()
    val doubts by viewModel.raguRaguState.collectAsState()
    val remainingSec by viewModel.remainingSeconds.collectAsState()
    val warnings by viewModel.cheatWarnings.collectAsState()

    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var showIndexDrawer by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()

    // Prevent random back clicks during active exams
    BackHandler(enabled = true) {
        Toast.makeText(context, "Harap selesaikan ujian terlebih dahulu melalui tombol kumpulkan.", Toast.LENGTH_SHORT).show()
    }

    // Advanced Local Anti-Cheat System
    // Monitors android activity Lifecycle transitions: e.g. when app goes background, we capture it as an exit infraction warning!
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.triggerTabSwitchWarning()
                Toast.makeText(
                    context,
                    "⚠️ PERINGATAN: Deteksi perubahan fokus layar! Pelanggaran dicatat oleh sistem pengawas.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (activeUjian == null || questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentSoal = questions.getOrNull(currentIndex) ?: questions.first()
    val currentSelectedAnswers = answers[currentSoal.id] ?: ""

    // Format timer
    val timerText = remember(remainingSec) {
        val mins = remainingSec / 60
        val secs = remainingSec % 60
        "%02d:%02d".format(mins, secs)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TOP STATUS NAV BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(
                        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Token & Index Indicator
                Column {
                    Text(
                        text = activeUjian!!.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "Soal ${currentIndex + 1} dari ${questions.size}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Middle Counter Timer Realtime limit
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (remainingSec < 120) Color(0xFFEF4444).copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Timer icon",
                        tint = if (remainingSec < 120) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timerText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (remainingSec < 120) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Right Quick Navigation Grid Drawer Icon
                IconButton(
                    onClick = { showIndexDrawer = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("exam_grid_toggle")
                ) {
                    Icon(imageVector = Icons.Default.GridView, contentDescription = "Buka navigasi soal")
                }
            }

            // Anti Cheat Warning Sub-banner
            if (warnings > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2))
                        .padding(vertical = 6.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Deteksi pindah tab: $warnings peringatan. Tindakan curang mengurangi integritas!",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // SOAL CARD AREA
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Difficulty tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when(currentSoal.difficulty) {
                                "Mudah" -> Color(0xFF10B981).copy(alpha = 0.1f)
                                "Sedang" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else -> Color(0xFFEF4444).copy(alpha = 0.1f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Kesulitan: ${currentSoal.difficulty}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when(currentSoal.difficulty) {
                                "Mudah" -> Color(0xFF10B981)
                                "Sedang" -> MaterialTheme.colorScheme.primary
                                else -> Color(0xFFEF4444)
                            }
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Question Text display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = currentSoal.questionText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // OPTION CHOICES SECTOR
                if (currentSoal.type == "pilihan_ganda") {
                    val options = listOf(
                        "A" to currentSoal.optionA,
                        "B" to currentSoal.optionB,
                        "C" to currentSoal.optionC,
                        "D" to currentSoal.optionD,
                        "E" to currentSoal.optionE
                    ).filter { it.second.isNotEmpty() }

                    options.forEach { (key, text) ->
                        val isSelected = currentSelectedAnswers == key
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { viewModel.selectAnswer(currentSoal.id, key) }
                                .testTag("option_$key"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                } else {
                    // ESSAY INPUT INTERFACE
                    Column {
                        Text(
                            text = "Tulis Jawaban Uraian Anda:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = currentSelectedAnswers,
                            onValueChange = { viewModel.selectAnswer(currentSoal.id, it) },
                            placeholder = { Text("Ketik jawaban esai secara lengkap di sini...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("essay_input_box"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 10,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Jawaban esai disimpan otomatis secara dinamis.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // PROGRESS BOTTOM NAVIGATION PANEL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
                    .border(BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(16.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                OutlinedButton(
                    onClick = { viewModel.prevQuestion() },
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kembali")
                }

                // Checkbox Ragu Ragu button
                val isDoubtful = doubts.contains(currentSoal.id)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { viewModel.toggleRaguRagu(currentSoal.id) }
                        .background(
                            if (isDoubtful) Color(0xFFF59E0B).copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isDoubtful) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("doubt_toggle_btn"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDoubtful,
                        onCheckedChange = { viewModel.toggleRaguRagu(currentSoal.id) },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF59E0B))
                    )
                    Text(
                        text = "Ragu-Ragu",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDoubtful) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Next or Submit trigger button
                if (currentIndex == questions.size - 1) {
                    Button(
                        onClick = { showSubmitConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("exam_submit_trigger_btn")
                    ) {
                        Text("Kumpulkan", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = { viewModel.nextQuestion() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Lanjut")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // FULL LIST DRAWAL NAVIGATION SHEET MODAL
        if (showIndexDrawer) {
            AlertDialog(
                onDismissRequest = { showIndexDrawer = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Peta Indeks Soal")
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Warna Hijau: Sudah Dijawab • Warna Kuning: Ragu-Ragu • Warna Abu: Belum Dijawab",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Wrapping grid item indices in beautiful flows
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            questions.forEachIndexed { idx, q ->
                                val answered = answers.containsKey(q.id) && answers[q.id]!!.isNotEmpty()
                                val inDoubt = doubts.contains(q.id)
                                val isCurrent = idx == currentIndex

                                val btnColor = when {
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    inDoubt -> Color(0xFFF59E0B)
                                    answered -> Color(0xFF10B981)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }

                                val txColor = when {
                                    isCurrent || inDoubt || answered -> Color.White
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(btnColor)
                                        .clickable {
                                            viewModel.jumpToQuestion(idx)
                                            showIndexDrawer = false
                                        }
                                        .testTag("jump_to_soal_$idx")
                                        .let {
                                            if (isCurrent) it.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)) else it
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (idx + 1).toString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = txColor
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showIndexDrawer = false }) {
                        Text("Tutup")
                    }
                }
            )
        }

        // CONFIRM SUBMISSION MODAL POPUP
        if (showSubmitConfirmDialog) {
            val unansweredCount = questions.size - answers.size
            AlertDialog(
                onDismissRequest = { showSubmitConfirmDialog = false },
                icon = { Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(36.dp)) },
                title = { Text("Kumpulkan Ujian?") },
                text = {
                    Column {
                        Text("Apakah Anda sudah yakin untuk mengakhiri pengerjaan lembar kerja ini?")
                        if (unansweredCount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "PENTING: Masih ada $unansweredCount soal yang belum Anda isi jawabannya!",
                                color = Color(0xFFDC2626),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        if (doubts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ada ${doubts.size} soal yang masih ditandai sebagai Ragu-Ragu.",
                                color = Color(0xFFD97706),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSubmitConfirmDialog = false
                            viewModel.submitExam {
                                onSubmitComplete()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Ya, Kumpulkan", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSubmitConfirmDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}
