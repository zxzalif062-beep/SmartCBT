package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // String: NIS for students, Email for Teacher/Admin
    val name: String,
    val role: String, // "siswa", "guru", "admin"
    val password: String,
    val className: String = "" // Class e.g., "XII-IPA-1" for students
)

@Entity(tableName = "ujians")
data class Ujian(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val code: String, // Exam token/code
    val durationMinutes: Int,
    val subject: String,
    val classTarget: String,
    val questionCount: Int,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "soals")
data class Soal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examId: Int,
    val questionText: String,
    val type: String, // "pilihan_ganda", "checkbox", "essay"
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val optionE: String = "",
    val correctAnswer: String, // multiple-choice option (A-E), multiple for checkboxes separated by comma, etc.
    val explanation: String = "",
    val difficulty: String = "Sedang" // "Mudah", "Sedang", "Sulit"
)

@Entity(tableName = "attempts")
data class StudentAttempt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examId: Int,
    val examTitle: String,
    val studentNis: String,
    val studentName: String,
    val score: Double = 0.0,
    val isCompleted: Boolean = false,
    val answersJson: String = "{}", // Map of question ID to chosen answer
    val raguRaguJson: String = "[]", // List of question IDs marked as doubtful
    val remainingSeconds: Int = 0,
    val warningsCount: Int = 0,
    val completedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "notices")
data class SchoolNotice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val summary: String,
    val timestamp: Long = System.currentTimeMillis()
)
