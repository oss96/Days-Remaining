package com.ossalali.daysremaining.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteColumnScope
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteImage
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.layout.RemoteText
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.clip
import androidx.compose.remote.creation.compose.modifier.contentDescription
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.height
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.semantics
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.shapes.RemoteRoundedCornerShape
import androidx.compose.remote.creation.compose.state.asRdp
import androidx.compose.remote.creation.compose.state.asRemoteTextUnit
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.remote.creation.compose.widgets.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.ossalali.daysremaining.R
import com.ossalali.daysremaining.model.EventItem
import com.ossalali.daysremaining.presentation.ui.theme.Dimensions
import kotlin.math.roundToInt

/** Minimum card width; the grid fits as many columns of this width as the widget allows. */
private val GRID_CELL_MIN_WIDTH = 140.dp
private val WIDGET_HORIZONTAL_PADDING = 12.dp

@SuppressLint("RestrictedApiAndroidX", "RestrictedApi")
@RemoteComposable
@Composable
fun EventWidgetContent(
    data: WidgetData,
    uiState: WidgetUiState,
    colors: WidgetColors,
    launch: (Intent) -> Unit,
    context: Context,
) {
  RemoteColumn(
      modifier =
          RemoteModifier.fillMaxSize()
              .clip(RemoteRoundedCornerShape(Dimensions.default.asRdp()))
              .background(colors.background.rc)
              .padding(horizontal = WIDGET_HORIZONTAL_PADDING.asRdp())
  ) {
    if (uiState.showTitleBar) {
      TitleBar(uiState = uiState, colors = colors, launch = launch, context = context)
    }

    if (data.eventItems.isEmpty()) {
      RemoteBox(
          modifier =
              RemoteModifier.fillMaxWidth().weight(1f.rf).onClick {
                launch(WidgetIntents.openApp(context))
              },
          contentAlignment = RemoteAlignment.Center,
      ) {
        RemoteText(
            text = context.getString(R.string.widget_no_events).rs,
            color = colors.onSurface.rc,
            fontSize = 12.rsp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
      }
    } else if (uiState.isSingleItem) {
      RemoteBox(
          modifier = RemoteModifier.fillMaxWidth().weight(1f.rf),
          contentAlignment = RemoteAlignment.Center,
      ) {
        EventItemCard(
            eventItem = data.eventItems.first(),
            customDateNotation = data.customDateNotation,
            uiState = uiState,
            colors = colors,
            launch = launch,
            context = context,
        )
      }
    } else {
      EventGrid(data = data, uiState = uiState, colors = colors, launch = launch, context = context)
    }
  }
}

@SuppressLint("RestrictedApiAndroidX", "RestrictedApi")
@RemoteComposable
@Composable
private fun TitleBar(
    uiState: WidgetUiState,
    colors: WidgetColors,
    launch: (Intent) -> Unit,
    context: Context,
) {
  RemoteBox(
      modifier = RemoteModifier.fillMaxWidth().padding(Dimensions.default.asRdp()),
      contentAlignment = RemoteAlignment.TopEnd,
  ) {
    RemoteRow(horizontalArrangement = RemoteArrangement.spacedBy(Dimensions.half.asRdp())) {
      // Every click re-renders the widget with freshly loaded events, so refresh needs no action.
      CircleIconButton(
          icon = R.drawable.baseline_refresh_24,
          contentDescription = "Refresh widget",
          uiState = uiState,
          colors = colors,
          context = context,
          onClick = {},
      )
      CircleIconButton(
          icon = R.drawable.outline_add_24,
          contentDescription = "Add event",
          uiState = uiState,
          colors = colors,
          context = context,
          onClick = { launch(WidgetIntents.addEvent(context)) },
      )
    }
  }
}

@SuppressLint("RestrictedApiAndroidX", "RestrictedApi")
@RemoteComposable
@Composable
private fun CircleIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    uiState: WidgetUiState,
    colors: WidgetColors,
    context: Context,
    onClick: () -> Unit,
) {
  // Vector painters emit expressions the Android 16 widget player can't evaluate, so the icon is
  // rasterized and tinted here.
  val iconSize = uiState.iconSize * 0.6f
  val iconSizePx = iconSize.toPx(context).roundToInt()
  val iconBitmap =
      remember(icon, iconSizePx, colors.onPrimaryContainer) {
        ContextCompat.getDrawable(context, icon)!!.mutate()
            .apply { setTint(colors.onPrimaryContainer.toArgb()) }
            .toBitmap(iconSizePx, iconSizePx)
            .asImageBitmap()
      }

  RemoteBox(
      modifier =
          RemoteModifier.sizeInPx(uiState.iconSize, context)
              // Percent-based corners (RemoteCircleShape) aren't drawn by the Android 16 player.
              .clip(RemoteRoundedCornerShape((uiState.iconSize / 2).asRdp()))
              .background(colors.primaryContainer.rc)
              .onClick(onClick)
              .semantics { this.contentDescription = contentDescription.rs },
      contentAlignment = RemoteAlignment.Center,
  ) {
    RemoteImage(
        remoteBitmap = iconBitmap.rb,
        contentDescription = null,
        modifier = RemoteModifier.sizeInPx(iconSize, context),
    )
  }
}

/**
 * Fixed sizes in dp are written as an operation the Android 16 widget player doesn't know, which
 * drops the element and everything after it, so they are converted to pixels here.
 */
private fun RemoteModifier.sizeInPx(size: Dp, context: Context): RemoteModifier {
  val px = size.toPx(context).rf
  return width(px).height(px)
}

private fun Dp.toPx(context: Context): Float = value * context.resources.displayMetrics.density

@RemoteComposable
@Composable
private fun RemoteColumnScope.EventGrid(
    data: WidgetData,
    uiState: WidgetUiState,
    colors: WidgetColors,
    launch: (Intent) -> Unit,
    context: Context,
) {
  val availableWidth = data.size.width - WIDGET_HORIZONTAL_PADDING * 2
  val columns = (availableWidth / GRID_CELL_MIN_WIDTH).toInt().coerceAtLeast(1)

  RemoteColumn(modifier = RemoteModifier.fillMaxWidth().weight(1f.rf)) {
    data.eventItems.take(uiState.maxItems).chunked(columns).forEach { rowItems ->
      RemoteRow(modifier = RemoteModifier.fillMaxWidth()) {
        rowItems.forEach { eventItem ->
          RemoteBox(modifier = RemoteModifier.weight(1f.rf)) {
            EventItemCard(
                eventItem = eventItem,
                customDateNotation = data.customDateNotation,
                uiState = uiState,
                colors = colors,
                launch = launch,
                context = context,
            )
          }
        }
        // Keep the last row's cards the same width as the others.
        repeat(columns - rowItems.size) { RemoteBox(modifier = RemoteModifier.weight(1f.rf)) {} }
      }
    }
  }
}

@SuppressLint("RestrictedApiAndroidX", "RestrictedApi")
@RemoteComposable
@Composable
private fun EventItemCard(
    eventItem: EventItem,
    customDateNotation: Boolean,
    uiState: WidgetUiState,
    colors: WidgetColors,
    launch: (Intent) -> Unit,
    context: Context,
) {
  RemoteColumn(
      modifier =
          RemoteModifier.padding(Dimensions.quarter.asRdp())
              .fillMaxWidth()
              .clip(RemoteRoundedCornerShape(Dimensions.default.asRdp()))
              .background(colors.secondaryContainer.rc)
              .onClick { launch(WidgetIntents.viewEvent(context, eventItem.id)) }
              .padding(Dimensions.half.asRdp()),
      verticalArrangement = RemoteArrangement.spacedBy(Dimensions.quarter.asRdp()),
      horizontalAlignment = RemoteAlignment.CenterHorizontally,
  ) {
    RemoteText(
        text = eventItem.title.rs,
        modifier = RemoteModifier.fillMaxWidth(),
        color = colors.primary.rc,
        fontSize = uiState.titleFontSize.asRemoteTextUnit(),
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
    )
    RemoteText(
        text = eventItem.getNumberOfDays(customDateNotation).rs,
        modifier = RemoteModifier.fillMaxWidth(),
        color = colors.onSecondaryContainer.rc,
        fontSize = uiState.daysFontSize.asRemoteTextUnit(),
        textAlign = TextAlign.Center,
    )
  }
}

/**
 * Material You colors, read from the system palette when the widget is rendered. Remote Compose's
 * light/dark themed colors need a newer player than the Android 16 widget profile, so a theme
 * switch shows up on the widget's next refresh.
 */
data class WidgetColors(
    val background: Color,
    val primary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val onSurface: Color,
) {
  companion object {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun resolve(context: Context): WidgetColors {
      val isDark =
          (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
              Configuration.UI_MODE_NIGHT_YES
      fun color(@ColorRes light: Int, @ColorRes dark: Int): Color =
          Color(context.getColor(if (isDark) dark else light))

      return WidgetColors(
          background =
              color(
                  android.R.color.system_surface_container_light,
                  android.R.color.system_surface_container_dark,
              ),
          primary =
              color(android.R.color.system_primary_light, android.R.color.system_primary_dark),
          primaryContainer =
              color(
                  android.R.color.system_primary_container_light,
                  android.R.color.system_primary_container_dark,
              ),
          onPrimaryContainer =
              color(
                  android.R.color.system_on_primary_container_light,
                  android.R.color.system_on_primary_container_dark,
              ),
          secondaryContainer =
              color(
                  android.R.color.system_secondary_container_light,
                  android.R.color.system_secondary_container_dark,
              ),
          onSecondaryContainer =
              color(
                  android.R.color.system_on_secondary_container_light,
                  android.R.color.system_on_secondary_container_dark,
              ),
          onSurface =
              color(
                  android.R.color.system_on_surface_light,
                  android.R.color.system_on_surface_dark,
              ),
      )
    }
  }
}
