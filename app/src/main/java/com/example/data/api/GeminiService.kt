package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Request Models for Gemini API
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

// Response Models for Gemini API
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiRepository {
    suspend fun generateQuestions(
        material: String,
        questionType: String, // "pilihan_ganda", "essay", "both"
        difficulty: String, // "Mudah", "Sedang", "Sulit"
        count: Int = 3
    ): String {
        val prompt = """
            Anda adalah asisten pembuat soal ujian CBT sekolah profesional (SmartCBT).
            Buatlah sebanyak $count soal ujian berdasarkan materi berikut ini secara tepat.
            
            MATERI:
            $material
            
            SPESIFIKASI SOAL:
            - Tipe Soal: $questionType (pilihan_ganda = harus ada opsi A, B, C, D, E; essay = masukkan teks pertanyaan dan kunci jawaban di correctAnswer)
            - Tingkat Kesulitan: $difficulty
            - Bahasa: Bahasa Indonesia yang baik dan benar (EYD terbaru)
            - Berikan pembahasan jawaban yang runut dan mendidik di bidang 'explanation' untuk membantu siswa belajar.
            
            Format Output HARUS berupa JSON Array murni yang valid tanpa format penanda markdown (JANGAN sertakan ```json ... ``` atau block sejenis) dengan properti item:
            - questionText: (string pertanyaan)
            - type: (isi dengan "pilihan_ganda" atau "essay")
            - optionA: (string untuk opsi A, kosongkan jika essay)
            - optionB: (string untuk opsi B, kosongkan jika essay)
            - optionC: (string untuk opsi C, kosongkan jika essay)
            - optionD: (string untuk opsi D, kosongkan jika essay)
            - optionE: (string untuk opsi E, kosongkan jika essay)
            - correctAnswer: (isi "A", "B", "C", "D", atau "E" jika pilihan ganda; isi kunci jawaban paragraf ringkas jika essay)
            - explanation: (pembahasan jawaban secara mendetail dan edukatif)
            - difficulty: "$difficulty"
            
            Pastikan output JSON valid dan siap diparse langsung. JANGAN menuliskan komentar, penjelasan lain, atau teks pembuka/penutup apapun di luar JSON array tersebut.
        """.trimIndent()

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "ERROR_API_KEY_MISSING"
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            generatedText ?: "[]"
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }
}
