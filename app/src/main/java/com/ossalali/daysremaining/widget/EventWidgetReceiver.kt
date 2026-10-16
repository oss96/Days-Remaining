package com.ossalali.daysremaining.widget

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.compose.remote.creation.compose.capture.captureSingleRemoteDocument
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.widgets.AbstractRCWidget
import androidx.compose.remote.creation.compose.widgets.RCWidget
import androidx.compose.remote.creation.compose.widgets.RemoteComposeWidget
import androidx.compose.remote.creation.compose.widgets.WidgetInformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.DpSize
import com.ossalali.daysremaining.R
import com.ossalali.daysremaining.model.EventItem
import com.ossalali.daysremaining.widget.di.WidgetRepositoryEntryPoint
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Everything one widget instance needs to render. */
data class WidgetData(
    val eventItems: List<EventItem>,
    val customDateNotation: Boolean,
    val size: DpSize,
)

/**
 * Home-screen widget. On Android 16+ it is drawn with Remote Compose ([EventWidgetContent]); older
 * versions get a plain RemoteViews layout ([buildFallbackRemoteViews]).
 *
 * [RemoteComposeWidget] is still a library-internal API in Remote Compose alpha, but it is the only
 * way to receive clicks from a Remote Compose widget. Google's own widget sample uses it the same
 * way. Rendering is done here rather than by the base class, see [captureRemoteViews].
 */
@SuppressLint("RestrictedApiAndroidX", "RestrictedApi")
class EventWidgetReceiver : RemoteComposeWidget() {

  override fun onUpdate(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetIds: IntArray,
  ) {
    // super.onUpdate would render right away on the main thread, before the events are loaded.
    renderAsync { appWidgetIds.forEach { render(context, appWidgetManager, it) } }
  }

  override fun onAppWidgetOptionsChanged(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int,
      newOptions: Bundle,
  ) {
    renderAsync { render(context, appWidgetManager, appWidgetId) }
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != AbstractRCWidget.ACTION) {
      super.onReceive(context, intent)
      return
    }
    // A click on a Remote Compose element. Its lambda only exists in a capture, so capture, run
    // the lambda and push a fresh capture.
    val appWidgetId = intent.getIntExtra("widgetId", AppWidgetManager.INVALID_APPWIDGET_ID)
    val lambdaId = intent.getIntExtra("id", -1)
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
    renderAsync {
      if (supportsRemoteCompose()) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        widgetData[appWidgetId] = loadWidgetData(context, appWidgetManager, appWidgetId)
        captureRemoteViews(context, appWidgetId)
        RemoteComposeWidgetInternals.runClickLambda(lambdaId)
        appWidgetManager.updateAppWidget(appWidgetId, captureRemoteViews(context, appWidgetId))
      }
    }
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    super.onDeleted(context, appWidgetIds)
    appWidgetIds.forEach { widgetData.remove(it) }
  }

  @RemoteComposable
  @Composable
  override fun Content(context: Context, widgetId: Int) {
    val data = widgetData[widgetId] ?: return
    if (!supportsRemoteCompose()) return
    EventWidgetContent(
        data = data,
        uiState = getWidgetUiState(data.size),
        colors = WidgetColors.resolve(context),
        launch = { intent -> launchActivity(context, intent) },
        context = context,
    )
  }

  private suspend fun render(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int,
  ) {
    val data = loadWidgetData(context, appWidgetManager, appWidgetId)
    Log.d(
        TAG,
        "Rendering widget $appWidgetId: ${data.eventItems.size} events at ${data.size}, " +
            "remoteCompose=${supportsRemoteCompose()}",
    )
    if (supportsRemoteCompose()) {
      widgetData[appWidgetId] = data
      appWidgetManager.updateAppWidget(appWidgetId, captureRemoteViews(context, appWidgetId))
    } else {
      appWidgetManager.updateAppWidget(appWidgetId, buildFallbackRemoteViews(context, data))
    }
  }

  /**
   * Captures [Content] into a Remote Compose document, with a click PendingIntent for each
   * clickable element.
   *
   * The base class would push the draw-instructions RemoteViews as they are, which One UI's
   * launcher drops ("skip updateAppWidget by old remoteView"). Nested in a regular layout they are
   * shown everywhere.
   */
  @RequiresApi(Build.VERSION_CODES.BAKLAVA)
  private suspend fun captureRemoteViews(context: Context, appWidgetId: Int): RemoteViews {
    val rcWidget = widget as RCWidget
    RemoteComposeWidgetInternals.clearClickLambdas()
    val document =
        captureSingleRemoteDocument(context = context, profile = rcWidget.profile) {
          CompositionLocalProvider(
              RemoteComposeWidgetInternals.localWidget() provides WidgetInformation(appWidgetId)
          ) {
            Content(context, appWidgetId)
          }
        }
    val drawInstructions = rcWidget.getRemoteView(context, document, this, appWidgetId)
    return RemoteViews(context.packageName, R.layout.widget_remote_compose).apply {
      addView(R.id.widget_remote_compose_root, drawInstructions)
    }
  }

  /** Runs [block] off the main thread, one render at a time, keeping the receiver alive. */
  private fun renderAsync(block: suspend () -> Unit) {
    val pendingResult = goAsync()
    scope.launch {
      try {
        renderLock.withLock { block() }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to render widget", e)
      } finally {
        pendingResult.finish()
      }
    }
  }

  private companion object {
    const val TAG = "EventWidgetReceiver"

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // RemoteComposeWidget tracks click lambdas in a process-wide map, so captures must not overlap.
    val renderLock = Mutex()

    // Content() is called back from inside the capture, so the data is staged here first.
    val widgetData = ConcurrentHashMap<Int, WidgetData>()

    /**
     * The library's widget profile (WIDGETS_V6) targets the Android 16 platform player. Android 15
     * can show RemoteViews draw instructions too, but not documents in that format.
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.BAKLAVA)
    fun supportsRemoteCompose(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA

    suspend fun loadWidgetData(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ): WidgetData {
      val entryPoint =
          EntryPointAccessors.fromApplication(
              context.applicationContext,
              WidgetRepositoryEntryPoint::class.java,
          )
      val selectedEventIds = entryPoint.widgetDataStore().getSelectedEventIds(appWidgetId).first()
      return WidgetData(
          eventItems = entryPoint.eventRepo().getActiveEventsByIds(selectedEventIds),
          customDateNotation = entryPoint.settingsRepo().customDateNotation.first(),
          size = appWidgetManager.getWidgetSize(appWidgetId),
      )
    }

    /**
     * Click lambdas run inside this receiver after the widget host forwards the tap, so the
     * activity start has to opt in to the host's background-activity-start privilege.
     */
    fun launchActivity(context: Context, intent: Intent) {
      if (!supportsRemoteCompose()) return
      val options =
          ActivityOptions.makeBasic()
              .setPendingIntentCreatorBackgroundActivityStartMode(
                  ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_ALWAYS
              )
      context.applicationContext.startActivity(intent, options.toBundle())
    }
  }
}
