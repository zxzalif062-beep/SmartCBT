package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Ujian::class, Soal::class, StudentAttempt::class, SchoolNotice::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun ujianDao(): UjianDao
    abstract fun soalDao(): SoalDao
    abstract fun studentAttemptDao(): StudentAttemptDao
    abstract fun schoolNoticeDao(): SchoolNoticeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_cbt_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { INSTANCE = it }
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            val userDao = db.userDao()
            val ujianDao = db.ujianDao()
            val soalDao = db.soalDao()
            val noticeDao = db.schoolNoticeDao()

            // Pre-seed mock accounts
            userDao.insertUser(User("12345", "Rifa'i Alif", "siswa", "siswa", "XII-IPA-1"))
            userDao.insertUser(User("67890", "Aila Az-Zahra", "siswa", "siswa", "XII-IPS-2"))
            userDao.insertUser(User("guru@cbt.com", "Pak Budi, S.Pd.", "guru", "guru"))
            userDao.insertUser(User("admin@cbt.com", "Admin Utama CBT", "admin", "admin"))

            // Pre-seed mock notification notices
            noticeDao.insertNotice(SchoolNotice(
                title = "Pengumuman Ujian Akhir Semester (UAS)",
                summary = "UAS akan dilaksanakan mulai tanggal 1 Juni. Mohon seluruh siswa mengunduh SmartCBT dan memverifikasi NIS masing-masing."
            ))
            noticeDao.insertNotice(SchoolNotice(
                title = "AI Question Generator Diaktifkan",
                summary = "Bapak/Ibu guru sekarang dapat membuat soal ujian secara otomatis dalam hitungan detik menggunakan asisten AI berbasis Kunci Materi."
            ))

            // Pre-seed school exams
            val examId1 = ujianDao.insertUjian(Ujian(
                title = "Penilaian Semester: Matematika Wajib",
                code = "MAT10",
                durationMinutes = 45,
                subject = "Matematika",
                classTarget = "XII-IPA-1",
                questionCount = 3,
                isActive = true
            )).toInt()

            val examId2 = ujianDao.insertUjian(Ujian(
                title = "Kuis Kilat: Bahasa Indonesia",
                code = "INDO12",
                durationMinutes = 30,
                subject = "Bahasa Indonesia",
                classTarget = "Semua Kelas XII",
                questionCount = 2,
                isActive = true
            )).toInt()

            // Pre-seed questions for Math
            soalDao.insertSoals(listOf(
                Soal(
                    examId = examId1,
                    questionText = "Berapakah hasil integral dari f(x) = 3x^2 + 2x + 1 ?",
                    type = "pilihan_ganda",
                    optionA = "x^3 + x^2 + x + C",
                    optionB = "3x^3 + 2x^2 + x + C",
                    optionC = "x^3 + 2x^2 + C",
                    optionD = "x^3 + x^2 + C",
                    optionE = "3x^3 + x^2 + x + C",
                    correctAnswer = "A",
                    explanation = "Integral dari 3x^2 adalah x^3, integral dari 2x adalah x^2, dan integral dari 1 adalah x. Jadi rumusnya adalah x^3 + x^2 + x + C.",
                    difficulty = "Sedang"
                ),
                Soal(
                    examId = examId1,
                    questionText = "Tentukan nilai x yang memenuhi persamaan kuadrat: x^2 - 5x + 6 = 0.",
                    type = "pilihan_ganda",
                    optionA = "x = 2 atau x = 3",
                    optionB = "x = -2 atau x = -3",
                    optionC = "x = 1 atau x = 6",
                    optionD = "x = -1 atau x = -6",
                    optionE = "x = 2 atau x = -3",
                    correctAnswer = "A",
                    explanation = "Persamaan dapat difaktorkan menjadi (x - 2)(x - 3) = 0. Sehingga akarnya adalah x = 2 atau x = 3.",
                    difficulty = "Mudah"
                ),
                Soal(
                    examId = examId1,
                    questionText = "Jelaskan konsep dasar teorema Pythagoras beserta rumus utamanya dalam segitiga siku-siku.",
                    type = "essay",
                    correctAnswer = "Teorema Pythagoras menyatakan bahwa kuadrat panjang sisi miring (hipotenusa) sama dengan jumlah kuadrat panjang kedua sisi siku-sikunya. Rumusnya: c^2 = a^2 + b^2.",
                    explanation = "Teorema ini berlaku hanya pada segitiga siku-siku untuk mencari sisi yang belum diketahui.",
                    difficulty = "Mudah"
                )
            ))

            // Pre-seed questions for Bahasa Indonesia
            soalDao.insertSoals(listOf(
                Soal(
                    examId = examId2,
                    questionText = "Manakah penulisan kalimat di bawah ini yang menggunakan ejaan bahasa Indonesia yang disempurnakan secara tepat?",
                    type = "pilihan_ganda",
                    optionA = "Ibu membeli pisang ambon di pasar.",
                    optionB = "Ibu membeli Pisang Ambon di Pasar.",
                    optionC = "Ibu membeli pisang Ambon di pasar.",
                    optionD = "Ibu membeli Pisang ambon di Pasar.",
                    optionE = "Ibu membeli pisang ambon Di Pasar.",
                    correctAnswer = "A",
                    explanation = "Pisang ambon adalah nama jenis buah, sehingga kata 'ambon' ditulis menggunakan huruf kecil. Tempat 'di pasar' dipisahkan dengan spasi.",
                    difficulty = "Sedang"
                ),
                Soal(
                    examId = examId2,
                    questionText = "Ide pokok merupakan gagasan atau masalah utama yang dibahas dalam suatu paragraf. Di manakah letak ide pokok di paragraf deduktif?",
                    type = "pilihan_ganda",
                    optionA = "Di akhir paragraf",
                    optionB = "Di awal paragraf",
                    optionC = "Di tengah paragraf",
                    optionD = "Di awal dan di akhir paragraf",
                    optionE = "Menyebar di seluruh paragraf",
                    correctAnswer = "B",
                    explanation = "Paragraf deduktif menyajikan kalimat utama yang mengandung ide pokok di awal paragraf, lalu diikuti dengan paragraf penjelas.",
                    difficulty = "Mudah"
                )
            ))
        }
    }
}
