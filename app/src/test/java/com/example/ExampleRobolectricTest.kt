package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.StrokeSynthesisEngine
import org.junit.Assert.assertEquals
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
    assertEquals("ArtHax", appName)
  }

  @Test
  fun `test vector stroke synthesis engine`() {
    val result = StrokeSynthesisEngine.synthesizeArtwork("Sekai Chibi Hatsune Miku")
    assertTrue(result.strokes.isNotEmpty())
    assertTrue(result.totalEstimatedPoints > 10)
  }
}

