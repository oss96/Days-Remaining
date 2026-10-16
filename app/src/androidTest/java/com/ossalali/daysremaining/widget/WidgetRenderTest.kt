package com.ossalali.daysremaining.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.compose.remote.creation.compose.capture.captureSingleRemoteDocument
import androidx.compose.remote.creation.profile.RcPlatformProfiles
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ossalali.daysremaining.R
import com.ossalali.daysremaining.model.EventItem
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Applies both widget renderings the way a launcher does and saves screenshots to the app's
 * external files dir (`widget-*.png`) for inspection.
 */
@RunWith(AndroidJUnit4::class)
class WidgetRenderTest {
  private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

  private val events =
      listOf(
          EventItem(
              id = 1,
              title = "Birthday",
              date = LocalDate.now().plusDays(12),
              description = "",
          ),
          EventItem(
              id = 2,
              title = "Holiday",
              date = LocalDate.now().plusDays(40),
              description = "",
          ),
          EventItem(id = 3, title = "Exam", date = LocalDate.now().minusDays(3), description = ""),
      )

  @Test
  fun fallbackRendersSelectedEvents() {
    val data = WidgetData(events, customDateNotation = false, size = DpSize(300.dp, 300.dp))
    val view = apply(buildFallbackRemoteViews(context, data), data.size, "widget-fallback.png")
    val items = view.findViewById<ViewGroup>(R.id.widget_items)
    assertEquals(View.VISIBLE, items.visibility)
    assertEquals(events.size, items.childCount)
  }

  @SuppressLint("RestrictedApiAndroidX", "RestrictedApi")
  @Test
  fun remoteComposeRendersAtEverySize() {
    assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA)
    val sizes =
        mapOf(
            "tiny" to DpSize(120.dp, 50.dp),
            "wide" to DpSize(300.dp, 120.dp),
            "tall" to DpSize(120.dp, 300.dp),
            "square" to DpSize(300.dp, 300.dp),
            "big" to DpSize(400.dp, 600.dp),
        )
    for ((name, size) in sizes) {
      for (eventItems in listOf(events, emptyList())) {
        val data = WidgetData(eventItems, customDateNotation = true, size = size)
        val document = runBlocking {
          captureSingleRemoteDocument(context = context, profile = RcPlatformProfiles.WIDGETS_V6) {
            EventWidgetContent(
                data = data,
                uiState = getWidgetUiState(size),
                colors = WidgetColors.resolve(context),
                launch = {},
                context = context,
            )
          }
        }
        val remoteViews =
            RemoteViews(RemoteViews.DrawInstructions.Builder(listOf(document.bytes)).build())
        val suffix = if (eventItems.isEmpty()) "-empty" else ""
        apply(remoteViews, size, "widget-rc-$name$suffix.png")
      }
    }
  }

  private fun apply(remoteViews: RemoteViews, size: DpSize, fileName: String): View {
    lateinit var view: View
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      val parent = FrameLayout(context)
      view = remoteViews.apply(context, parent)
      val width = size.width.value.toPx()
      val height = size.height.value.toPx()
      parent.addView(view, FrameLayout.LayoutParams(width, height))
      parent.measure(
          View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
          View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
      )
      parent.layout(0, 0, width, height)
      val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      parent.draw(Canvas(bitmap))
      File(context.getExternalFilesDir(null), fileName).outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
      }
    }
    return view
  }

  private fun Float.toPx(): Int =
      TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)
          .toInt()
}
