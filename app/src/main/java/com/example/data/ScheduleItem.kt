package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.RoseAccent
import org.json.JSONArray
import org.json.JSONObject

enum class RoutineCategory(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    WORKOUT("Fitness", Icons.Default.FitnessCenter, EmeraldAccent),
    MIND_FRESH("Mind Fresh", Icons.Default.FreeBreakfast, AmberAccent),
    DEEP_FOCUS("Deep Focus", Icons.Default.Code, EmeraldAccent),
    ROUTINE("Routine", Icons.Default.Restaurant, PurpleAccent),
    COMMUTE("Commute", Icons.Default.DirectionsCar, AmberAccent),
    OFFICE("Work / Office", Icons.Default.Work, RoseAccent),
    STUDY("Study / Prep", Icons.Default.School, EmeraldAccent),
    WALK("Walk & Reset", Icons.AutoMirrored.Filled.DirectionsWalk, EmeraldAccent),
    GAMING("Leisure / Chill", Icons.Default.Gamepad, PurpleAccent),
    MEDITATION("Mindfulness", Icons.Default.SelfImprovement, AmberAccent),
    SLEEP("Deep Sleep", Icons.Default.Bedtime, PurpleAccent)
}

enum class RoutinePreset(val displayName: String, val subtitle: String) {
    DEVELOPER("Developer & DSA", "11 blocks: Deep coding, office & workout"),
    STUDENT("Student & Exam Prep", "Focused study sprints, revision & breaks"),
    FITNESS("Health & Athlete", "Yoga, gym workout, balanced nutrition & rest"),
    REMOTE_WORK("Remote Professional", "Async sprints, deep work & work-life balance"),
    EARLY_BIRD("Early Bird 5 AM", "5 AM wake-up, meditation, journaling & deep focus"),
    NIGHT_OWL("Night Owl Creator", "Late morning start, nighttime creative coding"),
    WEEKEND("Weekend Reset", "Relaxed mornings, outdoor sports, hobbies & chill"),
    CUSTOM("Custom Schedule", "Start with clean custom activities")
}

data class TodoCheckItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isDone: Boolean = false
)

enum class TaskPriority(val title: String, val color: Color) {
    HIGH("High", RoseAccent),
    MEDIUM("Medium", AmberAccent),
    LOW("Normal", CyanAccent)
}

data class ScheduleItem(
    val id: Int,
    val start: String,
    val end: String,
    val activity: String,
    val desc: String,
    val category: RoutineCategory,
    val notes: String = "",
    val actualMinutesSpent: Int = 0,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val checklist: List<TodoCheckItem> = emptyList()
) {
    fun getStartMinutes(): Int = parseTimeToMinutes(start)
    fun getEndMinutes(): Int = parseTimeToMinutes(end)

    fun isActive(currentMinutes: Int): Boolean {
        val s = getStartMinutes()
        val e = getEndMinutes()
        return if (s < e) {
            currentMinutes in s until e
        } else {
            // Crosses midnight, e.g. 23:30 to 05:30
            currentMinutes >= s || currentMinutes < e
        }
    }

    fun getDurationMinutes(): Int {
        val s = getStartMinutes()
        val e = getEndMinutes()
        return if (s < e) {
            e - s
        } else {
            (1440 - s) + e
        }
    }

    fun getRemainingMinutes(currentMinutes: Int): Int {
        val s = getStartMinutes()
        val e = getEndMinutes()
        return if (s < e) {
            if (currentMinutes in s until e) e - currentMinutes else 0
        } else {
            if (currentMinutes >= s) {
                (1440 - currentMinutes) + e
            } else if (currentMinutes < e) {
                e - currentMinutes
            } else {
                0
            }
        }
    }

    fun getProgress(currentMinutes: Int): Float {
        val total = getDurationMinutes()
        if (total <= 0) return 0f
        val s = getStartMinutes()
        val e = getEndMinutes()

        val elapsed = if (s < e) {
            (currentMinutes - s).coerceIn(0, total)
        } else {
            if (currentMinutes >= s) {
                (currentMinutes - s).coerceIn(0, total)
            } else if (currentMinutes < e) {
                ((1440 - s) + currentMinutes).coerceIn(0, total)
            } else {
                0
            }
        }
        return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }

    fun formatDuration(): String {
        val minutes = getDurationMinutes()
        val hrs = minutes / 60
        val mins = minutes % 60
        return when {
            hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
            hrs > 0 -> "${hrs}h"
            else -> "${mins}m"
        }
    }

    fun formattedTimeRange12h(): String {
        return "${format12h(start)} - ${format12h(end)}"
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("start", start)
            put("end", end)
            put("activity", activity)
            put("desc", desc)
            put("category", category.name)
            put("notes", notes)
            put("actualMinutesSpent", actualMinutesSpent)
            put("priority", priority.name)
            val checkArr = JSONArray()
            checklist.forEach { item ->
                val c = JSONObject()
                c.put("id", item.id)
                c.put("text", item.text)
                c.put("isDone", item.isDone)
                checkArr.put(c)
            }
            put("checklist", checkArr)
        }
    }

    companion object {
        fun parseTimeToMinutes(time: String): Int {
            val parts = time.split(":")
            if (parts.size >= 2) {
                val hour = parts[0].trim().toIntOrNull() ?: 0
                val minute = parts[1].trim().toIntOrNull() ?: 0
                return hour * 60 + minute
            }
            return 0
        }

        fun format12h(time: String): String {
            val parts = time.split(":")
            if (parts.size >= 2) {
                val hour = parts[0].trim().toIntOrNull() ?: 0
                val minute = parts[1].trim().toIntOrNull() ?: 0
                val amPm = if (hour < 12) "AM" else "PM"
                val h12 = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                return String.format("%d:%02d %s", h12, minute, amPm)
            }
            return time
        }

        fun fromJson(json: JSONObject): ScheduleItem {
            val catName = json.optString("category", "ROUTINE")
            val cat = try {
                RoutineCategory.valueOf(catName)
            } catch (e: Exception) {
                RoutineCategory.ROUTINE
            }
            val prioName = json.optString("priority", "MEDIUM")
            val prio = try {
                TaskPriority.valueOf(prioName)
            } catch (e: Exception) {
                TaskPriority.MEDIUM
            }
            val checkList = mutableListOf<TodoCheckItem>()
            val arr = json.optJSONArray("checklist")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val c = arr.optJSONObject(i)
                    if (c != null) {
                        checkList.add(
                            TodoCheckItem(
                                id = c.optString("id", java.util.UUID.randomUUID().toString()),
                                text = c.optString("text", ""),
                                isDone = c.optBoolean("isDone", false)
                            )
                        )
                    }
                }
            }
            return ScheduleItem(
                id = json.optInt("id", (1..99999).random()),
                start = json.optString("start", "08:00"),
                end = json.optString("end", "09:00"),
                activity = json.optString("activity", "New Task"),
                desc = json.optString("desc", ""),
                category = cat,
                notes = json.optString("notes", ""),
                actualMinutesSpent = json.optInt("actualMinutesSpent", 0),
                priority = prio,
                checklist = checkList
            )
        }

        fun exportToJsonString(schedule: List<ScheduleItem>): String {
            val array = JSONArray()
            schedule.forEach { array.put(it.toJson()) }
            return array.toString(2)
        }

        fun importFromJsonString(jsonString: String): List<ScheduleItem>? {
            return try {
                val array = JSONArray(jsonString)
                val list = mutableListOf<ScheduleItem>()
                for (i in 0 until array.length()) {
                    list.add(fromJson(array.getJSONObject(i)))
                }
                if (list.isNotEmpty()) list else null
            } catch (e: Exception) {
                null
            }
        }

        val DEFAULT_SCHEDULE = listOf(
            ScheduleItem(
                id = 1,
                start = "05:30",
                end = "06:30",
                activity = "Exercise / Workout",
                desc = "Running, warm-up ya workout.",
                category = RoutineCategory.WORKOUT,
                notes = "Target: 5km run + 50 pushups & stretching"
            ),
            ScheduleItem(
                id = 2,
                start = "06:30",
                end = "07:00",
                activity = "Mind Fresh: Chai & Peace",
                desc = "Naha-dho kar fresh hona, chai/nashta, no screen.",
                category = RoutineCategory.MIND_FRESH,
                notes = "No smartphone screen; peaceful morning tea on balcony"
            ),
            ScheduleItem(
                id = 3,
                start = "07:00",
                end = "08:45",
                activity = "Practice Block 1 (Deep Coding)",
                desc = "1 hr 45 min deep focus on React/DSA.",
                category = RoutineCategory.DEEP_FOCUS,
                notes = "Focus: React state architecture & solve 2 DSA graph problems"
            ),
            ScheduleItem(
                id = 4,
                start = "08:45",
                end = "09:30",
                activity = "Ready & Breakfast",
                desc = "Final ready, packing bag, eating.",
                category = RoutineCategory.ROUTINE,
                notes = "High-protein breakfast, bag packed with laptop & charger"
            ),
            ScheduleItem(
                id = 5,
                start = "09:30",
                end = "10:00",
                activity = "Commute to Office",
                desc = "Travel to office.",
                category = RoutineCategory.COMMUTE,
                notes = "Listen to system design podcast during commute"
            ),
            ScheduleItem(
                id = 6,
                start = "10:00",
                end = "19:00",
                activity = "Office Hours",
                desc = "Office work and travel back.",
                category = RoutineCategory.OFFICE,
                notes = "Finish sprint tasks, attend standup, stay hydrated"
            ),
            ScheduleItem(
                id = 7,
                start = "19:00",
                end = "19:30",
                activity = "Mind Fresh: Evening Walk",
                desc = "Change clothes, 20 min walk, no screen.",
                category = RoutineCategory.WALK,
                notes = "Brisk outdoor walking in fresh air to reset mental energy"
            ),
            ScheduleItem(
                id = 8,
                start = "19:30",
                end = "20:30",
                activity = "Practice Block 2",
                desc = "1 hr coding review or exam prep.",
                category = RoutineCategory.DEEP_FOCUS,
                notes = "Code review, optimize algorithms, document learnings"
            ),
            ScheduleItem(
                id = 9,
                start = "20:30",
                end = "22:30",
                activity = "Gaming Zone",
                desc = "2 hours gaming and chilling.",
                category = RoutineCategory.GAMING,
                notes = "Chill gaming session with friends, pure relax mode"
            ),
            ScheduleItem(
                id = 10,
                start = "22:30",
                end = "23:30",
                activity = "Dinner & Wind Down",
                desc = "Family time, dinner, phone away by 11:15.",
                category = RoutineCategory.ROUTINE,
                notes = "Family dinner; put phone on charging desk by 11:15 PM"
            ),
            ScheduleItem(
                id = 11,
                start = "23:30",
                end = "05:30",
                activity = "Deep Sleep",
                desc = "Sound sleep.",
                category = RoutineCategory.SLEEP,
                notes = "Dark, cool room. Uninterrupted 6 hours deep recovery"
            )
        )

        val STUDENT_SCHEDULE = listOf(
            ScheduleItem(101, "06:00", "07:00", "Morning Revision", "Quick recall & formula revision", RoutineCategory.STUDY, "Revise flashcards and yesterday's notes"),
            ScheduleItem(102, "07:00", "08:00", "Workout & Breakfast", "Yoga, stretching & healthy meal", RoutineCategory.WORKOUT, "Light physical warmup"),
            ScheduleItem(103, "08:30", "13:30", "School / Lectures", "Core academic classes", RoutineCategory.STUDY, "Active note-taking in lectures"),
            ScheduleItem(104, "13:30", "14:30", "Lunch & Power Nap", "Nutritious meal & rest", RoutineCategory.MIND_FRESH, "20-minute power nap"),
            ScheduleItem(105, "15:00", "17:30", "Deep Study Block", "Problem solving & practice papers", RoutineCategory.STUDY, "Solve 2 mock exam papers"),
            ScheduleItem(106, "17:30", "18:30", "Outdoor Sports / Walk", "Physical play & fresh air", RoutineCategory.WALK, "Evening football or brisk walking"),
            ScheduleItem(107, "19:00", "21:00", "Homework & Project", "Assignments & weak-topic focus", RoutineCategory.STUDY, "Finish weekly problem set"),
            ScheduleItem(108, "21:00", "22:00", "Dinner & Relax", "Family time & light reading", RoutineCategory.ROUTINE, "No screens after 9:45 PM"),
            ScheduleItem(109, "22:00", "06:00", "Deep Sleep", "8 hours restorative sleep", RoutineCategory.SLEEP, "Sleep in quiet, dark room")
        )

        val FITNESS_SCHEDULE = listOf(
            ScheduleItem(201, "05:00", "06:30", "Gym Strength Training", "Heavy lifting & core exercises", RoutineCategory.WORKOUT, "Push day: Bench, overhead press, dips"),
            ScheduleItem(202, "06:30", "07:15", "Post-Workout Meal", "High protein shake & hydration", RoutineCategory.ROUTINE, "40g protein + electrolyte water"),
            ScheduleItem(203, "07:15", "08:00", "Cold Shower & Mind Prep", "Mental clarity & breathwork", RoutineCategory.MEDITATION, "10 min box breathing & gratitude"),
            ScheduleItem(204, "08:30", "13:00", "High Performance Work", "Deep professional work sprint", RoutineCategory.OFFICE, "High leverage business tasks"),
            ScheduleItem(205, "13:00", "14:00", "Clean Nutrition Lunch", "Balanced macros & micro nutrients", RoutineCategory.ROUTINE, "Grilled chicken/tofu + greens + quinoa"),
            ScheduleItem(206, "14:00", "17:30", "Afternoon Sprint", "Meetings, emails & planning", RoutineCategory.OFFICE, "Stand-up desk & 5 min walking breaks"),
            ScheduleItem(207, "18:00", "19:00", "Evening Cardio / Zone 2", "Incline walking or outdoor cycling", RoutineCategory.WORKOUT, "Zone 2 aerobic base cardio"),
            ScheduleItem(208, "19:30", "20:30", "Recovery & Sauna / Stretch", "Mobility work & foam rolling", RoutineCategory.MIND_FRESH, "Hamstring & hip mobility flow"),
            ScheduleItem(209, "20:30", "21:30", "Light Dinner & Magnesium", "Dinner & recovery nutrients", RoutineCategory.ROUTINE, "Salmon/lentils, chamomile tea"),
            ScheduleItem(210, "22:00", "05:00", "Optimal Recovery Sleep", "7 hours optimal sleep", RoutineCategory.SLEEP, "Room temp 19C, total darkness")
        )

        val REMOTE_WORK_SCHEDULE = listOf(
            ScheduleItem(301, "06:30", "07:30", "Morning Routine & Sun", "Sunlight exposure & brisk walk", RoutineCategory.WALK, "Get 15 min natural sunlight"),
            ScheduleItem(302, "07:30", "08:30", "Breakfast & Coffee", "Quiet coffee & reading", RoutineCategory.MIND_FRESH, "Read 10 pages of a book"),
            ScheduleItem(303, "08:30", "11:30", "Deep Focus Block 1", "Most critical task of the day", RoutineCategory.DEEP_FOCUS, "No Slack/email notifications allowed"),
            ScheduleItem(304, "11:30", "12:30", "Team Sync & Async Comms", "Team standup & Slack catchup", RoutineCategory.OFFICE, "Answer high-priority threads"),
            ScheduleItem(305, "12:30", "13:30", "Healthy Lunch & Unplug", "Cooking & eating away from desk", RoutineCategory.ROUTINE, "Step outside home office"),
            ScheduleItem(306, "13:30", "16:30", "Deep Focus Block 2", "Secondary project deliverables", RoutineCategory.DEEP_FOCUS, "Feature implementation sprint"),
            ScheduleItem(307, "16:30", "17:30", "Admin & Day Closeout", "Inbox zero & tomorrow's plan", RoutineCategory.OFFICE, "Write top 3 priorities for tomorrow"),
            ScheduleItem(308, "17:30", "19:00", "Home Workout / Running", "Sweat session & reset", RoutineCategory.WORKOUT, "45 min kettlebell & calisthenics"),
            ScheduleItem(309, "19:30", "21:30", "Dinner & Hobbies", "Cooking, gaming, music or family", RoutineCategory.GAMING, "Relax and disconnect from work"),
            ScheduleItem(310, "22:30", "06:30", "Deep Sleep", "8 hours sleep", RoutineCategory.SLEEP, "Phone charging in another room")
        )

        val EARLY_BIRD_SCHEDULE = listOf(
            ScheduleItem(401, "05:00", "05:30", "Wake Up & Hydrate", "Cold water, light stretching & gratitude", RoutineCategory.MEDITATION, "500ml water + 5 min gratitude journal"),
            ScheduleItem(402, "05:30", "06:30", "Morning Jog & Workout", "Sunrise run & bodyweight workout", RoutineCategory.WORKOUT, "4km easy pace outdoor jog"),
            ScheduleItem(403, "06:30", "07:30", "Shower & Healthy Breakfast", "Cold shower & nutritious meal", RoutineCategory.ROUTINE, "Oatmeal with nuts & fresh berries"),
            ScheduleItem(404, "07:30", "11:30", "Deep Focus Block (Peak Energy)", "Undivided focus on #1 priority", RoutineCategory.DEEP_FOCUS, "Tackle hardest cognitive task of the day"),
            ScheduleItem(405, "11:30", "12:30", "Midday Walk & Sunshine", "Nature walk & reset mind", RoutineCategory.WALK, "Step outside, eye rest from screens"),
            ScheduleItem(406, "12:30", "13:30", "Lunch & Light Reading", "Healthy lunch & book chapter", RoutineCategory.MIND_FRESH, "Read nonfiction book"),
            ScheduleItem(407, "13:30", "17:00", "Afternoon Execution & Meetings", "Collaborative tasks & admin work", RoutineCategory.OFFICE, "Clear messages & documentation"),
            ScheduleItem(408, "17:00", "18:00", "Creative Hobby / Skill", "Guitar, chess, or design practice", RoutineCategory.MIND_FRESH, "Creative hobbies"),
            ScheduleItem(409, "18:30", "19:30", "Family Dinner", "Quality time with loved ones", RoutineCategory.ROUTINE, "Zero device dinner"),
            ScheduleItem(410, "19:30", "21:30", "Wind Down & Journaling", "Stretching, chamomile tea & prep", RoutineCategory.MEDITATION, "Plan tomorrow's top 3 goals"),
            ScheduleItem(411, "21:30", "05:00", "Deep Recovery Sleep", "7.5 hours high quality sleep", RoutineCategory.SLEEP, "Room pitch dark, 18-20°C")
        )

        val NIGHT_OWL_SCHEDULE = listOf(
            ScheduleItem(501, "09:30", "10:30", "Brunch & Morning Fuel", "Slow wake up, coffee & eggs", RoutineCategory.MIND_FRESH, "Hot espresso & healthy brunch"),
            ScheduleItem(502, "10:30", "11:30", "Planning & Warmup", "Review tickets, plan sprint", RoutineCategory.ROUTINE, "Prioritize tasks for the night"),
            ScheduleItem(503, "11:30", "15:00", "Daytime Execution", "Meetings, code reviews & comms", RoutineCategory.OFFICE, "Finish all calls and syncs"),
            ScheduleItem(504, "15:00", "16:30", "Late Afternoon Workout", "Gym weights or swimming", RoutineCategory.WORKOUT, "High energy training session"),
            ScheduleItem(505, "16:30", "17:30", "Shower & Reset Walk", "Outdoor fresh air", RoutineCategory.WALK, "Brisk evening walk"),
            ScheduleItem(506, "17:30", "20:00", "Dinner & Chill", "Dinner with friends/family & gaming", RoutineCategory.GAMING, "Relax and disconnect"),
            ScheduleItem(507, "20:00", "00:00", "Night Deep Focus Sprint 1", "Peak silence: Architecture & coding", RoutineCategory.DEEP_FOCUS, "Zero distractions, uninterrupted flow"),
            ScheduleItem(508, "00:00", "00:30", "Mind Fresh Midnight Break", "Herbal tea, ambient music & stretch", RoutineCategory.MIND_FRESH, "Lo-fi music and green tea"),
            ScheduleItem(509, "00:30", "02:00", "Night Deep Focus Sprint 2", "Creative projects & problem solving", RoutineCategory.DEEP_FOCUS, "Complex algorithmic challenges"),
            ScheduleItem(510, "02:00", "09:30", "Deep Sleep", "7.5 hours restful sleep", RoutineCategory.SLEEP, "Blackout curtains & white noise")
        )

        val WEEKEND_SCHEDULE = listOf(
            ScheduleItem(601, "08:00", "09:00", "Peaceful Morning & Chai", "Sleep in slightly, breakfast & tea", RoutineCategory.MIND_FRESH, "Enjoy relaxing breakfast with music"),
            ScheduleItem(602, "09:00", "11:00", "Outdoor Hike / Cycling", "Active nature time & fresh air", RoutineCategory.WORKOUT, "Bike ride in park or trail hike"),
            ScheduleItem(603, "11:00", "13:30", "Passion Project / Side Hustle", "Building creative ideas with joy", RoutineCategory.DEEP_FOCUS, "Work on personal open-source app"),
            ScheduleItem(604, "13:30", "15:00", "Special Weekend Lunch", "Cook something special / dine out", RoutineCategory.ROUTINE, "Cooking favorite meal"),
            ScheduleItem(605, "15:00", "17:30", "Social Time & Catchup", "Friends, family or community meetup", RoutineCategory.MIND_FRESH, "Meet friends / family call"),
            ScheduleItem(606, "17:30", "19:00", "Sunset Walk & Coffee", "Golden hour stroll & podcast", RoutineCategory.WALK, "Listen to inspiring interview"),
            ScheduleItem(607, "19:00", "21:30", "Movie / Gaming Night", "Entertainment and pure fun", RoutineCategory.GAMING, "Watch movie or multiplayer game"),
            ScheduleItem(608, "21:30", "23:00", "Weekly Review & Dinner", "Reflect on wins, plan week ahead", RoutineCategory.ROUTINE, "Write weekly review & goals"),
            ScheduleItem(609, "23:00", "08:00", "Long Recovery Sleep", "Restorative 9 hours weekend sleep", RoutineCategory.SLEEP, "Full mental and physical recharge")
        )

        fun getScheduleForPreset(preset: RoutinePreset): List<ScheduleItem> {
            return when (preset) {
                RoutinePreset.DEVELOPER -> DEFAULT_SCHEDULE
                RoutinePreset.STUDENT -> STUDENT_SCHEDULE
                RoutinePreset.FITNESS -> FITNESS_SCHEDULE
                RoutinePreset.REMOTE_WORK -> REMOTE_WORK_SCHEDULE
                RoutinePreset.EARLY_BIRD -> EARLY_BIRD_SCHEDULE
                RoutinePreset.NIGHT_OWL -> NIGHT_OWL_SCHEDULE
                RoutinePreset.WEEKEND -> WEEKEND_SCHEDULE
                RoutinePreset.CUSTOM -> listOf(
                    ScheduleItem(1, "07:00", "08:00", "Morning Routine", "Start the day refreshed", RoutineCategory.ROUTINE, "Hydrate and stretch"),
                    ScheduleItem(2, "09:00", "13:00", "Deep Work Sprint", "Primary focus session", RoutineCategory.DEEP_FOCUS, "Top priority goal"),
                    ScheduleItem(3, "13:00", "14:00", "Lunch Break", "Nutritious meal", RoutineCategory.ROUTINE, "Rest and recharge"),
                    ScheduleItem(4, "14:00", "18:00", "Afternoon Session", "Secondary tasks and review", RoutineCategory.OFFICE, "Follow up and plan"),
                    ScheduleItem(5, "23:00", "07:00", "Night Rest", "Restorative sleep", RoutineCategory.SLEEP, "8 hours sleep")
                )
            }
        }
    }
}
