package com.ossalali.daysremaining.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.ossalali.daysremaining.MainActivity

object WidgetIntents {
  const val VIEW_EVENT_ACTION = "com.ossalali.daysremaining.action.VIEW_EVENT"
  const val ADD_EVENT_ACTION = "com.ossalali.daysremaining.action.ADD_EVENT"
  const val EVENT_ID = "EVENT_ID"

  fun openApp(context: Context): Intent = mainActivityIntent(context)

  fun addEvent(context: Context): Intent =
      mainActivityIntent(context).apply { action = ADD_EVENT_ACTION }

  fun viewEvent(context: Context, eventId: Int): Intent =
      mainActivityIntent(context).apply {
        action = VIEW_EVENT_ACTION
        putExtra(EVENT_ID, eventId)
      }

  /** Wraps [intent] for RemoteViews click handlers. [requestCode] must be unique per intent. */
  fun pendingIntent(context: Context, requestCode: Int, intent: Intent): PendingIntent =
      PendingIntent.getActivity(
          context,
          requestCode,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

  private fun mainActivityIntent(context: Context): Intent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
}

/** Re-renders every placed instance of the widget. */
fun refreshWidget(context: Context) {
  val appWidgetManager = AppWidgetManager.getInstance(context)
  val appWidgetIds =
      appWidgetManager.getAppWidgetIds(ComponentName(context, EventWidgetReceiver::class.java))
  if (appWidgetIds.isEmpty()) return
  refreshWidget(context, *appWidgetIds)
}

/** Re-renders the given widget instances. */
fun refreshWidget(context: Context, vararg appWidgetIds: Int) {
  val intent =
      Intent(context, EventWidgetReceiver::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
      }
  context.sendBroadcast(intent)
}
