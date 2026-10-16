package com.ossalali.daysremaining.widget

import android.appwidget.AppWidgetManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class WidgetUiState(
    val titleFontSize: TextUnit,
    val daysFontSize: TextUnit,
    val iconSize: Dp,
    val gridColumns: Int,
    val showTitleBar: Boolean,
    val isSingleItem: Boolean,
    val maxItems: Int = 14,
    val selectedSize: String,
)

private val TINY_SQUARE = DpSize(120.dp, 50.dp)
private val SMALL_SQUARE = DpSize(250.dp, 250.dp)
private val MEDIUM_SQUARE = DpSize(400.dp, 400.dp)
private val BIG_SQUARE = DpSize(400.dp, 500.dp)
private val EXTRA_BIG_SQUARE = DpSize(400.dp, 600.dp)
private val SMALL_HORIZONTAL_RECTANGLE = DpSize(250.dp, 120.dp)
private val VERTICAL_RECTANGLE = DpSize(120.dp, 300.dp)
private val LARGE_RECTANGLE = DpSize(300.dp, 250.dp)
private val LARGER_RECTANGLE = DpSize(300.dp, 400.dp)
private val TALLISH_RECTANGLE = DpSize(120.dp, 300.dp)
private val TALL_RECTANGLE = DpSize(120.dp, 500.dp)
private val TALLER_RECTANGLE = DpSize(120.dp, 600.dp)
private val WIDE_RECTANGLE = DpSize(300.dp, 120.dp)
private val WIDE_TALL_RECTANGLE = DpSize(300.dp, 300.dp)
private val WIDER_TALLER_RECTANGLE = DpSize(300.dp, 500.dp)
private val WIDER_EVEN_TALLER_RECTANGLE = DpSize(300.dp, 600.dp)

/**
 * Maps each defined DpSize to a specific UI configuration. The 'when' block is ordered from largest
 * to smallest to ensure the correct state is selected for the available space.
 */
fun getWidgetUiState(size: DpSize): WidgetUiState {
  return when {
    size.width >= EXTRA_BIG_SQUARE.width && size.height >= EXTRA_BIG_SQUARE.height ->
        WidgetUiState(
            titleFontSize = 20.sp,
            daysFontSize = 40.sp,
            iconSize = 50.dp,
            gridColumns = 2,
            showTitleBar = true,
            isSingleItem = false,
            maxItems = 10,
            selectedSize = "EXTRA_BIG_SQUARE",
        )
    size.width >= BIG_SQUARE.width && size.height >= BIG_SQUARE.height ->
        WidgetUiState(
            titleFontSize = 18.sp,
            daysFontSize = 36.sp,
            iconSize = 50.dp,
            gridColumns = 2,
            showTitleBar = true,
            isSingleItem = false,
            maxItems = 8,
            selectedSize = "BIG_SQUARE",
        )
    size.width >= MEDIUM_SQUARE.width && size.height >= MEDIUM_SQUARE.height ->
        WidgetUiState(
            titleFontSize = 16.sp,
            daysFontSize = 32.sp,
            iconSize = 50.dp,
            gridColumns = 2,
            showTitleBar = true,
            isSingleItem = false,
            maxItems = 6,
            selectedSize = "MEDIUM_SQUARE",
        )
    size.width >= WIDER_EVEN_TALLER_RECTANGLE.width &&
        size.height >= WIDER_EVEN_TALLER_RECTANGLE.height ->
        WidgetUiState(
            titleFontSize = 14.sp,
            daysFontSize = 28.sp,
            iconSize = 50.dp,
            gridColumns = 1,
            showTitleBar = true,
            isSingleItem = false,
            selectedSize = "WIDER_EVEN_TALLER_RECTANGLE",
        )
    size.width >= WIDER_TALLER_RECTANGLE.width && size.height >= WIDER_TALLER_RECTANGLE.height ->
        WidgetUiState(
            titleFontSize = 14.sp,
            daysFontSize = 28.sp,
            iconSize = 28.dp,
            gridColumns = 1,
            showTitleBar = true,
            isSingleItem = false,
            maxItems = 10,
            selectedSize = "WIDER_TALLER_RECTANGLE",
        )
    size.width >= WIDE_TALL_RECTANGLE.width && size.height >= WIDE_TALL_RECTANGLE.height ->
        WidgetUiState(
            titleFontSize = 14.sp,
            daysFontSize = 28.sp,
            iconSize = 28.dp,
            gridColumns = 2,
            showTitleBar = true,
            isSingleItem = false,
            maxItems = 8,
            selectedSize = "WIDE_TALL_RECTANGLE",
        )
    size.width >= TALLER_RECTANGLE.width && size.height >= TALLER_RECTANGLE.height ->
        WidgetUiState(
            titleFontSize = 14.sp,
            daysFontSize = 28.sp,
            iconSize = 28.dp,
            gridColumns = 1,
            showTitleBar = true,
            isSingleItem = false,
            maxItems = 7,
            selectedSize = "TALLER_RECTANGLE",
        )
    size.width >= TALL_RECTANGLE.width && size.height >= TALL_RECTANGLE.height ->
        WidgetUiState(
            titleFontSize = 14.sp,
            daysFontSize = 28.sp,
            iconSize = 28.dp,
            gridColumns = 1,
            showTitleBar = true,
            isSingleItem = false,
            maxItems = 5,
            selectedSize = "TALL_RECTANGLE",
        )
    size.width >= TALLISH_RECTANGLE.width && size.height >= TALLISH_RECTANGLE.height ->
        WidgetUiState(
            titleFontSize = 14.sp,
            daysFontSize = 28.sp,
            iconSize = 28.dp,
            gridColumns = 1,
            showTitleBar = true,
            isSingleItem = false,
            maxItems = 4,
            selectedSize = "TALLISH_RECTANGLE",
        )
    size.width >= WIDE_RECTANGLE.width &&
        size.height >= WIDE_RECTANGLE.height &&
        size.height < LARGE_RECTANGLE.height ->
        WidgetUiState(
            titleFontSize = 12.sp,
            daysFontSize = 22.sp,
            iconSize = 24.dp,
            gridColumns = 4,
            showTitleBar = false,
            isSingleItem = false,
            maxItems = 2,
            selectedSize = "WIDE_RECTANGLE",
        )
    size.width >= LARGER_RECTANGLE.width && size.height >= LARGER_RECTANGLE.height ->
        WidgetUiState(
            titleFontSize = 14.sp,
            daysFontSize = 28.sp,
            iconSize = 28.dp,
            gridColumns = 2,
            showTitleBar = true,
            isSingleItem = false,
            maxItems = 4,
            selectedSize = "LARGER_RECTANGLE",
        )
    size.width >= LARGE_RECTANGLE.width && size.height >= LARGE_RECTANGLE.height ->
        WidgetUiState(
            titleFontSize = 14.sp,
            daysFontSize = 28.sp,
            iconSize = 28.dp,
            gridColumns = 2,
            showTitleBar = true,
            isSingleItem = false,
            maxItems = 4,
            selectedSize = "LARGE_RECTANGLE",
        )
    size.height >= VERTICAL_RECTANGLE.height && size.width >= VERTICAL_RECTANGLE.width ->
        WidgetUiState(
            titleFontSize = 14.sp,
            daysFontSize = 24.sp,
            iconSize = 26.dp,
            gridColumns = 1,
            showTitleBar = true,
            isSingleItem = false,
            maxItems = 4,
            selectedSize = "VERTICAL_RECTANGLE",
        )
    size.width >= SMALL_SQUARE.width && size.height >= SMALL_SQUARE.height ->
        WidgetUiState(
            titleFontSize = 14.sp,
            daysFontSize = 26.sp,
            iconSize = 26.dp,
            gridColumns = 1,
            showTitleBar = true,
            isSingleItem = false,
            maxItems = 2,
            selectedSize = "SMALL_SQUARE",
        )

    size.width >= SMALL_HORIZONTAL_RECTANGLE.width &&
        size.height >= SMALL_HORIZONTAL_RECTANGLE.height ->
        WidgetUiState(
            titleFontSize = 12.sp,
            daysFontSize = 22.sp,
            iconSize = 0.dp,
            gridColumns = 2,
            showTitleBar = false,
            isSingleItem = true,
            maxItems = 1,
            selectedSize = "SMALL_HORIZONTAL_RECTANGLE",
        )
    size.width >= TINY_SQUARE.width && size.height >= TINY_SQUARE.height ->
        WidgetUiState(
            titleFontSize = 12.sp,
            daysFontSize = 20.sp,
            iconSize = 0.dp,
            gridColumns = 1,
            showTitleBar = false,
            isSingleItem = true,
            maxItems = 1,
            selectedSize = "TINY_SQUARE",
        )
    else ->
        WidgetUiState(
            titleFontSize = 12.sp,
            daysFontSize = 20.sp,
            iconSize = 0.dp,
            gridColumns = 1,
            showTitleBar = false,
            isSingleItem = true,
            maxItems = 1,
            selectedSize = "DEFAULT",
        )
  }
}

/**
 * The widget's current size in portrait orientation, which is what launchers show on phones. Falls
 * back to the minimum size from widget_info.xml before the host has reported one.
 */
fun AppWidgetManager.getWidgetSize(appWidgetId: Int): DpSize {
  val options = getAppWidgetOptions(appWidgetId)
  val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
  val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
  return if (width > 0 && height > 0) DpSize(width.dp, height.dp) else TINY_SQUARE
}
