package com.ossalali.daysremaining.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ossalali.daysremaining.widget.di.WidgetRepositoryEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

@AndroidEntryPoint
class WidgetPreferenceActivity : ComponentActivity() {
  private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setResult(RESULT_CANCELED)

    appWidgetId =
        intent
            ?.extras
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
      return
    }

    val appWidgetManager = AppWidgetManager.getInstance(this)
    val appWidgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId)
    val widgetEntryPoint =
        EntryPointAccessors.fromApplication(
            this.applicationContext,
            WidgetRepositoryEntryPoint::class.java,
        )
    val eventRepo = widgetEntryPoint.eventRepo()
    val settingsRepo = widgetEntryPoint.settingsRepo()
    val widgetDataStore = widgetEntryPoint.widgetDataStore()
    val viewModel =
        WidgetPreferenceScreenViewModel(
            eventRepository = eventRepo,
            settingsRepo = settingsRepo,
            widgetDataStore = widgetDataStore,
            appWidgetId = appWidgetId,
            appWidgetOptions = appWidgetOptions,
        )

    setContent {
      WidgetPreferenceScreen(viewModel = viewModel, onSaveComplete = { finishWithSuccess() })
    }
  }

  private fun finishWithSuccess() {
    val resultValue = Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) }
    setResult(RESULT_OK, resultValue)

    refreshWidget(this, appWidgetId)
    finish()
  }
}
