package com.ossalali.daysremaining.widget

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.ossalali.daysremaining.R

/**
 * The widget before Android 16, where Remote Compose documents can't be shown: a list of the
 * selected events, each opening its details. Tapping elsewhere opens the app.
 */
fun buildFallbackRemoteViews(context: Context, data: WidgetData): RemoteViews {
  val uiState = getWidgetUiState(data.size)
  val views = RemoteViews(context.packageName, R.layout.widget_fallback)
  views.setOnClickPendingIntent(
      R.id.widget_root,
      WidgetIntents.pendingIntent(context, 0, WidgetIntents.openApp(context)),
  )

  views.removeAllViews(R.id.widget_items)
  if (data.eventItems.isEmpty()) {
    views.setTextViewText(R.id.widget_message, context.getString(R.string.widget_no_events))
    views.setViewVisibility(R.id.widget_message, View.VISIBLE)
    views.setViewVisibility(R.id.widget_items, View.GONE)
    return views
  }

  views.setViewVisibility(R.id.widget_message, View.GONE)
  views.setViewVisibility(R.id.widget_items, View.VISIBLE)
  data.eventItems.take(uiState.maxItems).forEach { eventItem ->
    val item = RemoteViews(context.packageName, R.layout.widget_fallback_item)
    item.setTextViewText(R.id.widget_item_title, eventItem.title)
    item.setTextViewTextSize(
        R.id.widget_item_title,
        TypedValue.COMPLEX_UNIT_SP,
        uiState.titleFontSize.value,
    )
    item.setTextViewText(R.id.widget_item_days, eventItem.getNumberOfDays(data.customDateNotation))
    item.setTextViewTextSize(
        R.id.widget_item_days,
        TypedValue.COMPLEX_UNIT_SP,
        uiState.daysFontSize.value,
    )
    // The event id keeps each PendingIntent distinct; their intents differ only in extras.
    item.setOnClickPendingIntent(
        R.id.widget_item,
        WidgetIntents.pendingIntent(
            context,
            eventItem.id,
            WidgetIntents.viewEvent(context, eventItem.id),
        ),
    )
    views.addView(R.id.widget_items, item)
  }
  return views
}
