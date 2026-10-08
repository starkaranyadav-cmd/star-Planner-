package com.example

import com.example.data.RoutineCategory
import com.example.data.RoutinePreset
import com.example.data.ScheduleItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleItemTest {

    @Test
    fun parseTimeToMinutes_isCorrect() {
        assertEquals(330, ScheduleItem.parseTimeToMinutes("05:30"))
        assertEquals(420, ScheduleItem.parseTimeToMinutes("07:00"))
        assertEquals(0, ScheduleItem.parseTimeToMinutes("00:00"))
        assertEquals(1410, ScheduleItem.parseTimeToMinutes("23:30"))
    }

    @Test
    fun format12h_isCorrect() {
        assertEquals("5:30 AM", ScheduleItem.format12h("05:30"))
        assertEquals("7:00 PM", ScheduleItem.format12h("19:00"))
        assertEquals("12:00 AM", ScheduleItem.format12h("00:00"))
        assertEquals("11:30 PM", ScheduleItem.format12h("23:30"))
    }

    @Test
    fun daytimeActivity_isActiveCalculation() {
        val exercise = ScheduleItem(
            id = 1,
            start = "05:30",
            end = "06:30",
            activity = "Exercise / Workout",
            desc = "Workout",
            category = RoutineCategory.WORKOUT,
            notes = "Morning run"
        )

        // 05:29 -> not active
        assertFalse(exercise.isActive(329))
        // 05:30 -> active
        assertTrue(exercise.isActive(330))
        // 06:00 -> active
        assertTrue(exercise.isActive(360))
        // 06:30 -> not active
        assertFalse(exercise.isActive(390))
        assertEquals("Morning run", exercise.notes)
    }

    @Test
    fun editableNotes_savesDirectly() {
        val practiceBlock = ScheduleItem(
            id = 3,
            start = "07:00",
            end = "08:45",
            activity = "Practice Block 1",
            desc = "Deep Coding",
            category = RoutineCategory.DEEP_FOCUS,
            notes = "Initial thoughts"
        )

        val updated = practiceBlock.copy(notes = "LeetCode 2 questions & React Architecture")
        assertEquals("LeetCode 2 questions & React Architecture", updated.notes)
    }

    @Test
    fun overnightActivity_crossesMidnight_isActiveCalculation() {
        val deepSleep = ScheduleItem(
            id = 11,
            start = "23:30",
            end = "05:30",
            activity = "Deep Sleep",
            desc = "Sound sleep.",
            category = RoutineCategory.SLEEP
        )

        // 23:29 -> not active
        assertFalse(deepSleep.isActive(1409))
        // 23:30 -> active
        assertTrue(deepSleep.isActive(1410))
        // 00:05 (12:05 AM) -> active
        assertTrue(deepSleep.isActive(5))
        // 04:30 -> active
        assertTrue(deepSleep.isActive(270))
        // 05:29 -> active
        assertTrue(deepSleep.isActive(329))
        // 05:30 -> not active
        assertFalse(deepSleep.isActive(330))
    }

    @Test
    fun progressAndRemainingMinutes_calculateAccurately() {
        val practiceBlock = ScheduleItem(
            id = 3,
            start = "07:00",
            end = "08:45",
            activity = "Practice Block 1",
            desc = "Deep Coding",
            category = RoutineCategory.DEEP_FOCUS
        )

        assertEquals(105, practiceBlock.getDurationMinutes())
        assertEquals("1h 45m", practiceBlock.formatDuration())

        assertEquals(105, practiceBlock.getRemainingMinutes(420))
        assertEquals(0f, practiceBlock.getProgress(420), 0.01f)

        val at735 = 7 * 60 + 35
        assertEquals(70, practiceBlock.getRemainingMinutes(at735))
        assertEquals(0.333f, practiceBlock.getProgress(at735), 0.01f)
    }

    @Test
    fun defaultScheduleHas11Items() {
        assertEquals(11, ScheduleItem.DEFAULT_SCHEDULE.size)
        assertTrue(ScheduleItem.DEFAULT_SCHEDULE.first().notes.isNotEmpty())
    }

    @Test
    fun presetSchedulesLoadCorrectly() {
        val student = ScheduleItem.getScheduleForPreset(RoutinePreset.STUDENT)
        assertTrue(student.isNotEmpty())

        val fitness = ScheduleItem.getScheduleForPreset(RoutinePreset.FITNESS)
        assertTrue(fitness.isNotEmpty())

        val remote = ScheduleItem.getScheduleForPreset(RoutinePreset.REMOTE_WORK)
        assertTrue(remote.isNotEmpty())
    }
}
