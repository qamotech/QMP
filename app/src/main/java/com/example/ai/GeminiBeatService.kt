package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiBeatResponse(
    val title: String = "AI Beat",
    val genre: String = "Hip Hop",
    val bpm: Int = 120,
    val scale: String = "Minor",
    val tracks: List<AiTrackPattern> = emptyList()
)

data class AiTrackPattern(
    val trackType: String, // KICK, SNARE, HIHAT, PERC, BASS, SYNTH, PADS
    val activeSteps: List<Boolean> = emptyList(), // 16 items
    val volume: Float = 0.8f
)

class GeminiBeatService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateBeatFromPrompt(userPrompt: String): AiBeatResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackBeatPattern(userPrompt)
        }

        val systemPrompt = """
            You are an expert music producer and drum machine programmer.
            User Prompt: "$userPrompt"
            
            Generate a 16-step beat pattern JSON response for an Android Beatbox & Synth Studio app.
            
            Strict JSON Schema:
            {
              "title": "Short Beat Title",
              "genre": "Genre Name",
              "bpm": 120,
              "scale": "Minor",
              "tracks": [
                { "trackType": "KICK", "activeSteps": [true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false], "volume": 0.9 },
                { "trackType": "SNARE", "activeSteps": [false, false, false, false, true, false, false, false, false, false, false, false, true, false, false, false], "volume": 0.8 },
                { "trackType": "HIHAT", "activeSteps": [true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false], "volume": 0.7 },
                { "trackType": "PERC", "activeSteps": [false, false, true, false, false, false, false, true, false, false, true, false, false, false, false, false], "volume": 0.6 },
                { "trackType": "BASS", "activeSteps": [true, false, false, true, false, false, true, false, true, false, false, true, false, false, true, false], "volume": 0.85 },
                { "trackType": "SYNTH", "activeSteps": [false, true, false, false, true, false, false, true, false, true, false, false, true, false, false, false], "volume": 0.75 },
                { "trackType": "PADS", "activeSteps": [true, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false], "volume": 0.7 }
              ]
            }
            Return ONLY raw JSON. No markdown formatting.
        """.trimIndent()

        val requestObj = JSONObject()
        val contentsArr = JSONArray()
        val contentObj = JSONObject()
        val partsArr = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", systemPrompt)
        partsArr.put(partObj)
        contentObj.put("parts", partsArr)
        contentsArr.put(contentObj)
        requestObj.put("contents", contentsArr)

        try {
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestObj.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful || bodyString.isEmpty()) {
                return@withContext fallbackBeatPattern(userPrompt)
            }

            // Extract candidate JSON
            val jsonTextStart = bodyString.indexOf("{")
            val jsonTextEnd = bodyString.lastIndexOf("}")
            if (jsonTextStart >= 0 && jsonTextEnd > jsonTextStart) {
                val candidateJson = bodyString.substring(jsonTextStart, jsonTextEnd + 1)
                val rootJson = JSONObject(candidateJson)

                // Try candidates array from Gemini REST payload if returned full object
                val textContent = if (rootJson.has("candidates")) {
                    val cand = rootJson.getJSONArray("candidates").getJSONObject(0)
                    cand.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                } else {
                    candidateJson
                }

                val cleanStart = textContent.indexOf("{")
                val cleanEnd = textContent.lastIndexOf("}")
                if (cleanStart >= 0 && cleanEnd > cleanStart) {
                    val parsedObj = JSONObject(textContent.substring(cleanStart, cleanEnd + 1))
                    val title = parsedObj.optString("title", "AI Custom Beat")
                    val genre = parsedObj.optString("genre", "AI Groove")
                    val bpm = parsedObj.optInt("bpm", 120)
                    val scale = parsedObj.optString("scale", "Minor")

                    val trackPatterns = mutableListOf<AiTrackPattern>()
                    if (parsedObj.has("tracks")) {
                        val tracksArr = parsedObj.getJSONArray("tracks")
                        for (i in 0 until tracksArr.length()) {
                            val tObj = tracksArr.getJSONObject(i)
                            val tType = tObj.optString("trackType", "KICK")
                            val vol = tObj.optDouble("volume", 0.8).toFloat()

                            val stepsList = mutableListOf<Boolean>()
                            if (tObj.has("activeSteps")) {
                                val stepsArr = tObj.getJSONArray("activeSteps")
                                for (s in 0 until stepsArr.length()) {
                                    stepsList.add(stepsArr.optBoolean(s, false))
                                }
                            }
                            trackPatterns.add(AiTrackPattern(tType, stepsList, vol))
                        }
                    }

                    return@withContext AiBeatResponse(title, genre, bpm, scale, trackPatterns)
                }
            }

            return@withContext fallbackBeatPattern(userPrompt)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext fallbackBeatPattern(userPrompt)
        }
    }

    private fun fallbackBeatPattern(prompt: String): AiBeatResponse {
        val lower = prompt.lowercase()
        val bpm = when {
            lower.contains("trap") -> 140
            lower.contains("lo-fi") || lower.contains("lofi") -> 85
            lower.contains("techno") || lower.contains("house") -> 128
            lower.contains("dubstep") -> 150
            else -> 118
        }

        val genre = when {
            lower.contains("trap") -> "Trap Heavy"
            lower.contains("lo-fi") || lower.contains("lofi") -> "Lo-Fi Chillhop"
            lower.contains("techno") -> "Futuristic Techno"
            lower.contains("cyber") || lower.contains("synth") -> "Cyberpunk Synthwave"
            else -> "AI Custom Grooves"
        }

        val kick = MutableList(16) { i -> i % 4 == 0 }
        val snare = MutableList(16) { i -> i % 8 == 4 }
        val hihat = MutableList(16) { i -> i % 2 == 0 }
        val perc = MutableList(16) { i -> i == 2 || i == 7 || i == 11 }
        val bass = MutableList(16) { i -> i == 0 || i == 3 || i == 8 || i == 10 }
        val synth = MutableList(16) { i -> i == 1 || i == 5 || i == 9 || i == 13 }
        val pads = MutableList(16) { i -> i == 0 || i == 8 }

        return AiBeatResponse(
            title = "Beat: " + prompt.take(20),
            genre = genre,
            bpm = bpm,
            scale = "Minor",
            tracks = listOf(
                AiTrackPattern("KICK", kick, 0.9f),
                AiTrackPattern("SNARE", snare, 0.8f),
                AiTrackPattern("HIHAT", hihat, 0.7f),
                AiTrackPattern("PERC", perc, 0.6f),
                AiTrackPattern("BASS", bass, 0.85f),
                AiTrackPattern("SYNTH", synth, 0.75f),
                AiTrackPattern("PADS", pads, 0.7f)
            )
        )
    }
}
