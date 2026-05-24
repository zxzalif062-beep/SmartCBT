package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiRepository
import com.example.data.database.*
import com.example.data.repository.AppRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class CbtViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(db)
    private val geminiRepository = GeminiRepository()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // Authentication States
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Data Flows
    val allUjians: StateFlow<List<Ujian>> = repository.getAllUjiansFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotices: StateFlow<List<SchoolNotice>> = repository.getAllNoticesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttempts: StateFlow<List<StudentAttempt>> = repository.getAllAttemptsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter attempts based on Role
    private val _studentAttempts = MutableStateFlow<List<StudentAttempt>>(emptyList())
    val studentAttempts: StateFlow<List<StudentAttempt>> = _studentAttempts.asStateFlow()

    // Exam-Taking (Ujian) Session States
    private val _activeUjian = MutableStateFlow<Ujian?>(null)
    val activeUjian: StateFlow<Ujian?> = _activeUjian.asStateFlow()

    private val _activeQuestions = MutableStateFlow<List<Soal>>(emptyList())
    val activeQuestions: StateFlow<List<Soal>> = _activeQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    // AnswersState is a map of question ID to chosen code (A, B, C, D, E, or typed essay string)
    private val _answersState = MutableStateFlow<Map<Int, String>>(emptyMap())
    val answersState: StateFlow<Map<Int, String>> = _answersState.asStateFlow()

    // Ragu-Ragu state is a set of question IDs
    private val _raguRaguState = MutableStateFlow<Set<Int>>(emptySet())
    val raguRaguState: StateFlow<Set<Int>> = _raguRaguState.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _cheatWarnings = MutableStateFlow(0)
    val cheatWarnings: StateFlow<Int> = _cheatWarnings.asStateFlow()

    private val _examFinished = MutableStateFlow(false)
    val examFinished: StateFlow<Boolean> = _examFinished.asStateFlow()

    private val _activeResultState = MutableStateFlow<StudentAttempt?>(null)
    val activeResultState: StateFlow<StudentAttempt?> = _activeResultState.asStateFlow()

    // UI Configuration & Dark Mode state
    val isDarkMode = MutableStateFlow(false)

    // AI Generative State
    private val _aiStatus = MutableStateFlow<String>("IDLE") // "IDLE", "LOADING", "SUCCESS", "ERROR", "API_KEY_MISSING"
    val aiStatus: StateFlow<String> = _aiStatus.asStateFlow()

    private val _generatedQuestions = MutableStateFlow<List<Soal>>(emptyList())
    val generatedQuestions: StateFlow<List<Soal>> = _generatedQuestions.asStateFlow()

    private var timerJob: Job? = null

    private var attemptsJob: Job? = null

    init {
        // Collect student attempts reactively when currentUser changes
        viewModelScope.launch {
            currentUser.collect { user ->
                attemptsJob?.cancel()
                if (user != null && user.role == "siswa") {
                    attemptsJob = viewModelScope.launch {
                        repository.getAttemptsForStudentFlow(user.id).collect { list ->
                            _studentAttempts.value = list
                        }
                    }
                } else {
                    _studentAttempts.value = emptyList()
                }
            }
        }
    }

    // Login Action
    fun login(idInput: String, passwordInput: String, onLogged: (String) -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            val user = repository.getUser(idInput.trim())
            if (user != null && user.password == passwordInput) {
                _currentUser.value = user
                onLogged(user.role)
            } else {
                _authError.value = "Username (NIS/Email) atau password salah."
            }
        }
    }

    // Register Action
    fun register(idInput: String, nameInput: String, passwordInput: String, role: String, className: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            val existing = repository.getUser(idInput.trim())
            if (existing != null) {
                _authError.value = "User dengan NIS/Email tersebut sudah terdaftar."
            } else {
                val newUser = User(
                    id = idInput.trim(),
                    name = nameInput.trim(),
                    password = passwordInput,
                    role = role,
                    className = className
                )
                repository.registerUser(newUser)
                _currentUser.value = newUser
                onSuccess()
            }
        }
    }

    fun logout() {
        cancelTimer()
        _currentUser.value = null
        _activeUjian.value = null
        _activeQuestions.value = emptyList()
        _currentQuestionIndex.value = 0
        _answersState.value = emptyMap()
        _raguRaguState.value = emptySet()
        _examFinished.value = false
        _activeResultState.value = null
    }

    // Forgot password simulation
    fun resetPassword(idInput: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUser(idInput.trim())
            if (user != null) {
                onResult("Password Anda adalah: '${user.password}'. Harap catat dan login kembali.")
            } else {
                onResult("Akun dengan ID/NIS tersebut tidak ditemukan.")
            }
        }
    }

    // Join Exam by Token Code
    fun joinExamByCode(code: String, onJoinSuccess: () -> Unit, onJoinError: (String) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val ujian = repository.getUjianByCode(code.uppercase().trim())
            if (ujian == null) {
                onJoinError("Token ujian tidak valid.")
                return@launch
            }
            if (!ujian.isActive) {
                onJoinError("Ujian ini sedang dinonaktifkan.")
                return@launch
            }

            // Check if student has already completed this exam
            val previousAttempt = repository.getAttempt(ujian.id, user.id)
            if (previousAttempt != null && previousAttempt.isCompleted) {
                onJoinError("Anda sudah menyelesaikan ujian ini.")
                return@launch
            }

            // Lock in-progress state or create new attempt
            _activeUjian.value = ujian
            val questions = repository.getSoalsForExam(ujian.id)
            _activeQuestions.value = questions
            _currentQuestionIndex.value = 0
            _examFinished.value = false
            _cheatWarnings.value = 0

            // Base answers reconstitution
            if (previousAttempt != null) {
                _cheatWarnings.value = previousAttempt.warningsCount
                _remainingSeconds.value = previousAttempt.remainingSeconds
                
                // Parse existing answers
                _answersState.value = parseAnswersJson(previousAttempt.answersJson)
                _raguRaguState.value = parseRaguRaguJson(previousAttempt.raguRaguJson)
            } else {
                _remainingSeconds.value = ujian.durationMinutes * 60
                _answersState.value = emptyMap()
                _raguRaguState.value = emptySet()

                // Register attempt in DB
                val initialAttempt = StudentAttempt(
                    examId = ujian.id,
                    examTitle = ujian.title,
                    studentNis = user.id,
                    studentName = user.name,
                    remainingSeconds = _remainingSeconds.value,
                    answersJson = "{}",
                    raguRaguJson = "[]"
                )
                repository.saveAttempt(initialAttempt)
            }

            // Start ticking timer
            startTimer()
            onJoinSuccess()
        }
    }

    // Timer Implementation
    private fun startTimer() {
        cancelTimer()
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0 && !_examFinished.value) {
                delay(1000)
                _remainingSeconds.value -= 1
                
                // Save progress every 15 seconds automatically
                if (_remainingSeconds.value % 15 == 0) {
                    saveCurrentProgress()
                }
            }
            if (_remainingSeconds.value <= 0 && !_examFinished.value) {
                // Auto submit when time runs out
                submitExam()
            }
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // Save exam process state to database in real-time
    private suspend fun saveCurrentProgress() {
        val user = _currentUser.value ?: return
        val exam = _activeUjian.value ?: return
        val currentAttempt = repository.getAttempt(exam.id, user.id)
        if (currentAttempt != null) {
            val updated = currentAttempt.copy(
                answersJson = serializeAnswersMap(_answersState.value),
                raguRaguJson = serializeRaguRaguSet(_raguRaguState.value),
                remainingSeconds = _remainingSeconds.value,
                warningsCount = _cheatWarnings.value
            )
            repository.updateAttempt(updated)
        }
    }

    // Answer question
    fun selectAnswer(questionId: Int, answer: String) {
        val current = _answersState.value.toMutableMap()
        current[questionId] = answer
        _answersState.value = current

        // Save immediately as part of Auto-save feature
        viewModelScope.launch {
            saveCurrentProgress()
        }
    }

    // Toggle doubt / ragu-ragu
    fun toggleRaguRagu(questionId: Int) {
        val current = _raguRaguState.value.toMutableSet()
        if (current.contains(questionId)) {
            current.remove(questionId)
        } else {
            current.add(questionId)
        }
        _raguRaguState.value = current

        viewModelScope.launch {
            saveCurrentProgress()
        }
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _activeQuestions.value.size - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun prevQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun jumpToQuestion(index: Int) {
        if (index in 0 until _activeQuestions.value.size) {
            _currentQuestionIndex.value = index
        }
    }

    // Anticheat: tab switch detected / screen focus lost
    fun triggerTabSwitchWarning() {
        val user = _currentUser.value
        val exam = _activeUjian.value
        if (user != null && exam != null && !_examFinished.value) {
            _cheatWarnings.value += 1
            viewModelScope.launch {
                saveCurrentProgress()
            }
        }
    }

    // Submit Exam
    fun submitExam(onFinished: (StudentAttempt) -> Unit = {}) {
        cancelTimer()
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val exam = _activeUjian.value ?: return@launch
            val questions = _activeQuestions.value
            val answers = _answersState.value

            // Auto-grading Choice Questions
            var correctCount = 0
            var choiceQuestionsCount = 0
            questions.forEach { q ->
                if (q.type == "pilihan_ganda") {
                    choiceQuestionsCount++
                    val studentAns = answers[q.id]?.trim()?.uppercase() ?: ""
                    val correctAns = q.correctAnswer.trim().uppercase()
                    if (studentAns == correctAns) {
                        correctCount++
                    }
                } else {
                    // Simulating AI/Teacher grading essay as "filled in" = score
                    val studentAns = answers[q.id]?.trim() ?: ""
                    if (studentAns.isNotEmpty() && studentAns.length > 10) {
                        correctCount++
                        choiceQuestionsCount++
                    } else if (studentAns.isNotEmpty()) {
                        choiceQuestionsCount++
                        // partial credits
                    } else {
                        choiceQuestionsCount++
                    }
                }
            }

            // Scale score precisely
            val rawScore = if (choiceQuestionsCount > 0) {
                (correctCount.toDouble() / choiceQuestionsCount.toDouble()) * 100.0
            } else {
                100.0
            }

            val finalScore = "%.1f".format(rawScore).replace(",", ".").toDoubleOrNull() ?: rawScore

            val attempt = repository.getAttempt(exam.id, user.id)
            if (attempt != null) {
                val updated = attempt.copy(
                    score = finalScore,
                    isCompleted = true,
                    answersJson = serializeAnswersMap(answers),
                    raguRaguJson = serializeRaguRaguSet(_raguRaguState.value),
                    remainingSeconds = 0,
                    warningsCount = _cheatWarnings.value,
                    completedDate = System.currentTimeMillis()
                )
                repository.updateAttempt(updated)
                _activeResultState.value = updated
                _examFinished.value = true
                onFinished(updated)
            } else {
                val newAttempt = StudentAttempt(
                    examId = exam.id,
                    examTitle = exam.title,
                    studentNis = user.id,
                    studentName = user.name,
                    score = finalScore,
                    isCompleted = true,
                    answersJson = serializeAnswersMap(answers),
                    raguRaguJson = serializeRaguRaguSet(_raguRaguState.value),
                    remainingSeconds = 0,
                    warningsCount = _cheatWarnings.value,
                    completedDate = System.currentTimeMillis()
                )
                val attemptId = repository.saveAttempt(newAttempt)
                val attemptWithId = newAttempt.copy(id = attemptId.toInt())
                _activeResultState.value = attemptWithId
                _examFinished.value = true
                onFinished(attemptWithId)
            }
            _activeUjian.value = null // Resets exam screen
        }
    }

    // Review past result
    fun reviewAttemptResult(attempt: StudentAttempt, onReviewReady: () -> Unit) {
        viewModelScope.launch {
            _activeResultState.value = attempt
            val questions = repository.getSoalsForExam(attempt.examId)
            _activeQuestions.value = questions
            _answersState.value = parseAnswersJson(attempt.answersJson)
            _raguRaguState.value = parseRaguRaguJson(attempt.raguRaguJson)
            onReviewReady()
        }
    }

    // Teacher & Admin Management Methods
    fun addNewUjian(title: String, code: String, durationMin: Int, subject: String, classTarget: String, soals: List<Soal>, onCompleted: () -> Unit) {
        viewModelScope.launch {
            val token = code.uppercase().trim()
            val newUjian = Ujian(
                title = title.trim(),
                code = token,
                durationMinutes = durationMin,
                subject = subject.trim(),
                classTarget = classTarget,
                questionCount = soals.size
            )
            val examId = repository.createUjian(newUjian)
            
            // Link questions
            val populatedQuestions = soals.map { it.copy(examId = examId) }
            repository.addSoals(populatedQuestions)
            onCompleted()
        }
    }

    fun deleteUjian(ujian: Ujian) {
        viewModelScope.launch {
            repository.deleteUjian(ujian)
        }
    }

    // User admin control actions
    fun addOrUpdateUserAdmin(idInput: String, nameInput: String, pass: String, role: String, className: String) {
        viewModelScope.launch {
            val user = User(idInput.trim(), nameInput.trim(), role, pass, className)
            repository.registerUser(user)
        }
    }

    fun deleteUserAdmin(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }

    // AI Generative Question builder
    fun generateAIQuestions(
        materialText: String,
        questionType: String, // "pilihan_ganda", "essay"
        difficulty: String, // "Mudah", "Sedang", "Sulit"
        count: Int
    ) {
        viewModelScope.launch {
            _aiStatus.value = "LOADING"
            _generatedQuestions.value = emptyList()

            val rawJsonResult = geminiRepository.generateQuestions(
                material = materialText,
                questionType = questionType,
                difficulty = difficulty,
                count = count
            )

            if (rawJsonResult == "ERROR_API_KEY_MISSING") {
                _aiStatus.value = "API_KEY_MISSING"
                return@launch
            } else if (rawJsonResult.startsWith("ERROR")) {
                _aiStatus.value = "ERROR"
                return@launch
            }

            try {
                val parsed = parseSoalListJson(rawJsonResult)
                if (parsed.isNotEmpty()) {
                    _generatedQuestions.value = parsed
                    _aiStatus.value = "SUCCESS"
                } else {
                    _aiStatus.value = "ERROR"
                }
            } catch (e: Exception) {
                _aiStatus.value = "ERROR"
            }
        }
    }

    fun clearGeneratedAiQuestions() {
        _generatedQuestions.value = emptyList()
        _aiStatus.value = "IDLE"
    }

    // JSON Helper conversions
    private fun serializeAnswersMap(answers: Map<Int, String>): String {
        return try {
            val type = Types.newParameterizedType(Map::class.java, Integer::class.java, String::class.java)
            val adapter = moshi.adapter<Map<Int, String>>(type)
            adapter.toJson(answers)
        } catch (e: Exception) {
            "{}"
        }
    }

    private fun parseAnswersJson(json: String): Map<Int, String> {
        if (json.isEmpty() || json == "{}") return emptyMap()
        return try {
            val type = Types.newParameterizedType(Map::class.java, Integer::class.java, String::class.java)
            val adapter = moshi.adapter<Map<Int, String>>(type)
            adapter.fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun serializeRaguRaguSet(raguSet: Set<Int>): String {
        return try {
            val type = Types.newParameterizedType(Set::class.java, Integer::class.java)
            val adapter = moshi.adapter<Set<Int>>(type)
            adapter.toJson(raguSet)
        } catch (e: Exception) {
            "[]"
        }
    }

    private fun parseRaguRaguJson(json: String): Set<Int> {
        if (json.isEmpty() || json == "[]") return emptySet()
        return try {
            val type = Types.newParameterizedType(Set::class.java, Integer::class.java)
            val adapter = moshi.adapter<Set<Int>>(type)
            adapter.fromJson(json) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun parseSoalListJson(jsonString: String): List<Soal> {
        val sanitized = jsonString.trim()
        return try {
            // Raw JSON structures mapping
            val type = Types.newParameterizedType(List::class.java, Map::class.java)
            val adapter = moshi.adapter<List<Map<String, Any>>>(type)
            val list = adapter.fromJson(sanitized) ?: emptyList()
            
            list.map { map ->
                Soal(
                    examId = 0,
                    questionText = map["questionText"] as? String ?: "",
                    type = map["type"] as? String ?: "pilihan_ganda",
                    optionA = map["optionA"] as? String ?: "",
                    optionB = map["optionB"] as? String ?: "",
                    optionC = map["optionC"] as? String ?: "",
                    optionD = map["optionD"] as? String ?: "",
                    optionE = map["optionE"] as? String ?: "",
                    correctAnswer = map["correctAnswer"] as? String ?: "A",
                    explanation = map["explanation"] as? String ?: "",
                    difficulty = map["difficulty"] as? String ?: "Sedang"
                )
            }
        } catch (e: java.lang.Exception) {
            // Simple backup custom parser if Moshi fails or model responds slightly off format
            parseSoalsManuallyFallback(sanitized)
        }
    }

    private fun parseSoalsManuallyFallback(jsonString: String): List<Soal> {
        // Simple manual parser inside ViewModel for resiliency
        val list = mutableListOf<Soal>()
        try {
            val items = jsonString.split("},").map { it.replace("[", "").replace("]", "").trim() }
            for (item in items) {
                val qTxt = getJsonVal(item, "questionText")
                val type = getJsonVal(item, "type").ifEmpty { "pilihan_ganda" }
                val optA = getJsonVal(item, "optionA")
                val optB = getJsonVal(item, "optionB")
                val optC = getJsonVal(item, "optionC")
                val optD = getJsonVal(item, "optionD")
                val optE = getJsonVal(item, "optionE")
                val ans = getJsonVal(item, "correctAnswer")
                val exp = getJsonVal(item, "explanation")
                val diff = getJsonVal(item, "difficulty").ifEmpty { "Sedang" }

                if (qTxt.isNotEmpty()) {
                    list.add(Soal(
                        examId = 0,
                        questionText = qTxt,
                        type = type,
                        optionA = optA,
                        optionB = optB,
                        optionC = optC,
                        optionD = optD,
                        optionE = optE,
                        correctAnswer = ans,
                        explanation = exp,
                        difficulty = diff
                    ))
                }
            }
        } catch (_: Exception) {}
        return list
    }

    private fun getJsonVal(json: String, key: String): String {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1) ?: ""
    }
}
