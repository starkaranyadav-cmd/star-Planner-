package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ScheduleItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Daily Routine", appName)
  }

  @Test
  fun `export and import routine json with android framework`() {
    val original = ScheduleItem.DEFAULT_SCHEDULE
    val jsonString = ScheduleItem.exportToJsonString(original)
    assertTrue(jsonString.contains("Exercise / Workout"))

    val imported = ScheduleItem.importFromJsonString(jsonString)
    assertNotNull(imported)
    assertEquals(original.size, imported!!.size)
    assertEquals(original.first().activity, imported.first().activity)
  }
}
