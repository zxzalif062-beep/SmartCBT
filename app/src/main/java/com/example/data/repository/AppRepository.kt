package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val ujianDao = db.ujianDao()
    private val soalDao = db.soalDao()
    private val attemptDao = db.studentAttemptDao()
    private val noticeDao = db.schoolNoticeDao()

    // Users
    fun getAllUsersFlow(): Flow<List<User>> = userDao.getAllUsers()
    suspend fun getUser(id: String): User? = userDao.getUserById(id)
    suspend fun registerUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)

    // Exams (Ujian)
    fun getAllUjiansFlow(): Flow<List<Ujian>> = ujianDao.getAllUjians()
    suspend fun getUjianByCode(code: String): Ujian? = ujianDao.getUjianByCode(code)
    suspend fun getUjianById(id: Int): Ujian? = ujianDao.getUjianById(id)
    suspend fun createUjian(ujian: Ujian): Int {
        return ujianDao.insertUjian(ujian).toInt()
    }
    suspend fun updateUjian(ujian: Ujian) = ujianDao.updateUjian(ujian)
    suspend fun deleteUjian(ujian: Ujian) {
        soalDao.deleteSoalsForExam(ujian.id)
        ujianDao.deleteUjian(ujian)
    }

    // Questions (Soal)
    fun getSoalsForExamFlow(examId: Int): Flow<List<Soal>> = soalDao.getSoalsForExamFlow(examId)
    suspend fun getSoalsForExam(examId: Int): List<Soal> = soalDao.getSoalsForExam(examId)
    suspend fun addSoal(soal: Soal): Int = soalDao.insertSoal(soal).toInt()
    suspend fun addSoals(soals: List<Soal>) = soalDao.insertSoals(soals)
    suspend fun deleteSoalsForExam(examId: Int) = soalDao.deleteSoalsForExam(examId)
    suspend fun deleteSoal(soal: Soal) = soalDao.deleteSoal(soal)

    // Attempts
    fun getAllAttemptsFlow(): Flow<List<StudentAttempt>> = attemptDao.getAllAttempts()
    fun getAttemptsForStudentFlow(nis: String): Flow<List<StudentAttempt>> = attemptDao.getAttemptsForStudent(nis)
    fun getAttemptsForExamFlow(examId: Int): Flow<List<StudentAttempt>> = attemptDao.getAttemptsForExam(examId)
    suspend fun getAttempt(examId: Int, nis: String): StudentAttempt? = attemptDao.getAttempt(examId, nis)
    suspend fun saveAttempt(attempt: StudentAttempt): Long = attemptDao.insertAttempt(attempt)
    suspend fun updateAttempt(attempt: StudentAttempt) = attemptDao.updateAttempt(attempt)
    suspend fun deleteAttempt(id: Int) = attemptDao.deleteAttemptById(id)

    // School Notices (Pengumuman)
    fun getAllNoticesFlow(): Flow<List<SchoolNotice>> = noticeDao.getAllNotices()
    suspend fun addNotice(notice: SchoolNotice) = noticeDao.insertNotice(notice)
    suspend fun deleteNotice(notice: SchoolNotice) = noticeDao.deleteNotice(notice)
}
