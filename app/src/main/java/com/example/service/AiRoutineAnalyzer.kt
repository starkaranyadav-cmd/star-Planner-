package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.ScheduleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class DailyAnalysisResult(
    val score: Int,
    val summary: String,
    val highlights: List<String>,
    val improvementTips: List<String>,
    val nextAction: String,
    val isAiGenerated: Boolean
)

class AiRoutineAnalyzer {

    companion object {
        private const val TAG = "AiRoutineAnalyzer"
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeDailyRoutine(
        schedule: List<ScheduleItem>,
        completedIds: Set<Int>,
        activeItem: ScheduleItem?,
        currentTimeStr: String
    ): DailyAnalysisResult = withContext(Dispatchers.IO) {
        val completedCount = completedIds.size
        val totalCount = schedule.size
        val completionRate = if (totalCount > 0) (completedCount * 100) / totalCount else 0

        val completedNames = schedule.filter { completedIds.contains(it.id) }.map { it.activity }
        val pendingNames = schedule.filter { !completedIds.contains(it.id) }.map { "${it.activity} (${it.start}-${it.end})" }

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "No valid Gemini API key found, generating comprehensive heuristic analysis")
            return@withContext generateHeuristicAnalysis(
                schedule, completedCount, totalCount, completionRate, activeItem
            )
        }

        try {
            val prompt = buildPrompt(
                schedule, completedNames, pendingNames, completionRate, activeItem, currentTimeStr
            )

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)

                val generationConfig = JSONObject().apply {
                    put("temperature", 0.7)
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.w(TAG, "Gemini API failed with HTTP ${response.code}: $errorBody")
                return@withContext generateHeuristicAnalysis(
                    schedule, completedCount, totalCount, completionRate, activeItem
                )
            }

            val responseBody = response.body?.string() ?: ""
            val parsedResult = parseGeminiResponse(responseBody)
            parsedResult ?: generateHeuristicAnalysis(
                schedule, completedCount, totalCount, completionRate, activeItem
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Gemini API call", e)
            generateHeuristicAnalysis(
                schedule, completedCount, totalCount, completionRate, activeItem
            )
        }
    }

    private fun buildPrompt(
        schedule: List<ScheduleItem>,
        completedNames: List<String>,
        pendingNames: List<String>,
        completionRate: Int,
        activeItem: ScheduleItem?,
        currentTimeStr: String
    ): String {
        return """
You are an expert productivity coach. Analyze the user's daily routine & productivity tracking data.
Current Time: $currentTimeStr
Active Activity: ${activeItem?.activity ?: "None / Transition"}
Total Scheduled Blocks: ${schedule.size}
Completed Tasks ($completionRate%): ${completedNames.joinToString(", ").ifEmpty { "None yet" }}
Pending Tasks: ${pendingNames.joinToString("; ").ifEmpty { "All completed!" }}

The user's core daily schedule includes:
- Morning: Exercise (05:30-06:30), Chai & Peace (06:30-07:00), Practice Block 1 (Deep Coding, 07:00-08:45), Breakfast (08:45-09:30)
- Day: Commute & Office Hours (10:00-19:00)
- Evening & Night: Evening Walk (19:00-19:30), Practice Block 2 (Coding/Review, 19:30-20:30), Gaming Zone (20:30-22:30), Dinner & Wind Down (22:30-23:30), Deep Sleep (23:30-05:30)

Return your response strictly in the following JSON format without Markdown code blocks:
{
  "score": <Integer between 40 and 98 based on completion and balanced routine>,
  "summary": "<2-3 encouraging sentences analyzing their day in an energetic, helpful Hinglish/English style>",
  "highlights": [
    "<Key positive accomplishment 1>",
    "<Key positive accomplishment 2>"
  ],
  "improvementTips": [
    "<Actionable tip 1>",
    "<Actionable tip 2>",
    "<Actionable tip 3>"
  ],
  "nextAction": "<One immediate high-impact action to do right now>"
}
""".trimIndent()
    }

    private fun parseGeminiResponse(jsonString: String): DailyAnalysisResult? {
        try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val content = candidates.getJSONObject(0).optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val text = parts.getJSONObject(0).optString("text", "")

            // Strip potential markdown ```json blocks
            val cleaned = text
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val analysisJson = JSONObject(cleaned)
            val score = analysisJson.optInt("score", 80)
            val summary = analysisJson.optString("summary", "Great progress on your routine today!")

            val highlights = mutableListOf<String>()
            val highlightsArray = analysisJson.optJSONArray("highlights")
            if (highlightsArray != null) {
                for (i in 0 until highlightsArray.length()) {
                    highlights.add(highlightsArray.getString(i))
                }
            }

            val improvementTips = mutableListOf<String>()
            val tipsArray = analysisJson.optJSONArray("improvementTips")
            if (tipsArray != null) {
                for (i in 0 until tipsArray.length()) {
                    improvementTips.add(tipsArray.getString(i))
                }
            }

            val nextAction = analysisJson.optString(
                "nextAction", "Complete the next upcoming routine block on time!"
            )

            return DailyAnalysisResult(
                score = score.coerceIn(0, 100),
                summary = summary,
                highlights = if (highlights.isEmpty()) listOf("Maintained daily discipline", "Structured deep work blocks") else highlights,
                improvementTips = if (improvementTips.isEmpty()) listOf("Take 5-minute eye breaks during coding", "Ensure screens are off by 23:15 for restorative sleep") else improvementTips,
                nextAction = nextAction,
                isAiGenerated = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini response JSON", e)
            return null
        }
    }

    private fun generateHeuristicAnalysis(
        schedule: List<ScheduleItem>,
        completedCount: Int,
        totalCount: Int,
        completionRate: Int,
        activeItem: ScheduleItem?
    ): DailyAnalysisResult {
        val calculatedScore = (50 + (completionRate * 0.45)).toInt().coerceIn(55, 96)

        val summary = when {
            completionRate >= 80 -> "Outstanding execution today! You have checked off almost all core habits and kept momentum high across workouts and deep coding."
            completionRate >= 50 -> "Solid consistency so far! You're halfway through today's productivity roadmap. Keep your energy up for the evening practice session."
            completionRate > 0 -> "Good start! You have checked off initial routines. Focus on closing out the active and upcoming blocks with full dedication."
            else -> "Routine active! You have a well-balanced roadmap planned for workout, deep coding, and unwind time. Start checking off your blocks as you complete them!"
        }

        val highlights = mutableListOf<String>()
        if (completedCount > 0) {
            highlights.add("Checked off $completedCount out of $totalCount daily routine milestones")
        }
        highlights.add("Balanced plan: 2h 45m of Deep Coding practice combined with workout & outdoor walk")
        highlights.add("Protected 6-hour restorative sleep window (23:30 to 05:30)")

        val tips = listOf(
            "Screen Curfew: Ensure phone and laptop are away by 23:15 to maximize deep sleep cycles.",
            "Deep Focus: Keep your phone in Focus Mode during Practice Blocks to prevent DSA context-switching.",
            "Hydration & Posture: Take 2-minute stretch breaks during 9 hours of office work."
        )

        val nextAction = activeItem?.let {
            "Give 100% focus to current: ${it.activity} (${it.start} - ${it.end})"
        } ?: "Prepare your mindset for the next routine block."

        return DailyAnalysisResult(
            score = calculatedScore,
            summary = summary,
            highlights = highlights,
            improvementTips = tips,
            nextAction = nextAction,
            isAiGenerated = false
        )
    }
}
