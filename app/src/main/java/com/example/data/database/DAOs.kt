package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface UjianDao {
    @Query("SELECT * FROM ujians ORDER BY id DESC")
    fun getAllUjians(): Flow<List<Ujian>>

    @Query("SELECT * FROM ujians WHERE code = :code LIMIT 1")
    suspend fun getUjianByCode(code: String): Ujian?

    @Query("SELECT * FROM ujians WHERE id = :id LIMIT 1")
    suspend fun getUjianById(id: Int): Ujian?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUjian(ujian: Ujian): Long

    @Update
    suspend fun updateUjian(ujian: Ujian)

    @Delete
    suspend fun deleteUjian(ujian: Ujian)
}

@Dao
interface SoalDao {
    @Query("SELECT * FROM soals WHERE examId = :examId ORDER BY id ASC")
    fun getSoalsForExamFlow(examId: Int): Flow<List<Soal>>

    @Query("SELECT * FROM soals WHERE examId = :examId ORDER BY id ASC")
    suspend fun getSoalsForExam(examId: Int): List<Soal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoal(soal: Soal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoals(soals: List<Soal>)

    @Query("DELETE FROM soals WHERE examId = :examId")
    suspend fun deleteSoalsForExam(examId: Int)

    @Delete
    suspend fun deleteSoal(soal: Soal)
}

@Dao
interface StudentAttemptDao {
    @Query("SELECT * FROM attempts ORDER BY id DESC")
    fun getAllAttempts(): Flow<List<StudentAttempt>>

    @Query("SELECT * FROM attempts WHERE studentNis = :nis ORDER BY id DESC")
    fun getAttemptsForStudent(nis: String): Flow<List<StudentAttempt>>

    @Query("SELECT * FROM attempts WHERE examId = :examId")
    fun getAttemptsForExam(examId: Int): Flow<List<StudentAttempt>>

    @Query("SELECT * FROM attempts WHERE examId = :examId AND studentNis = :nis LIMIT 1")
    suspend fun getAttempt(examId: Int, nis: String): StudentAttempt?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: StudentAttempt): Long

    @Update
    suspend fun updateAttempt(attempt: StudentAttempt)

    @Query("DELETE FROM attempts WHERE id = :id")
    suspend fun deleteAttemptById(id: Int)
}

@Dao
interface SchoolNoticeDao {
    @Query("SELECT * FROM notices ORDER BY timestamp DESC")
    fun getAllNotices(): Flow<List<SchoolNotice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: SchoolNotice)

    @Delete
    suspend fun deleteNotice(notice: SchoolNotice)
}
