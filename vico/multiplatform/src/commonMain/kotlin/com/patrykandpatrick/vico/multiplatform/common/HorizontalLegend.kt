/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatrick.vico.multiplatform.common

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import kotlin.math.max

/**
 * [HorizontalLegend] displays legend items beside one another in lines.
 *
 * @property items adds the [LegendItem]s.
 * @property iconSize the [LegendItem.icon] size.
 * @property iconLabelSpacing the spacing between [LegendItem.icon] and [LegendItem.labelComponent].
 * @property rowSpacing the row spacing.
 * @property columnSpacing the column spacing.
 * @property padding the content padding.
 */
public open class HorizontalLegend<M : MeasuringContext, D : DrawingContext>(
  protected val items: AdditionScope<LegendItem>.(ExtraStore) -> Unit,
  protected val iconSize: Dp = Defaults.LEGEND_ICON_SIZE.dp,
  protected val iconLabelSpacing: Dp = Defaults.LEGEND_ICON_LABEL_SPACING.dp,
  protected val rowSpacing: Dp = Defaults.LEGEND_ROW_SPACING.dp,
  protected val columnSpacing: Dp = Defaults.LEGEND_COLUMN_SPACING.dp,
  protected val padding: Insets = Insets.Zero,
) : Legend<M, D> {
  private val itemManager = LegendItemManager(items)
  private val heights = mutableListOf<Float>()
  private val lines = mutableListOf<MutableList<LegendItem>>(mutableListOf())
  override var bounds: Rect = Rect.Zero

  override fun getHeight(context: M, maxWidth: Float): Float =
    with(context) {
      itemManager.addItems(this)
      if (itemManager.itemList.isEmpty()) return@with 0f
      lines.clear()
      lines.add(mutableListOf())
      var height =
        max(
          itemManager.itemList
            .first()
            .getLabelHeight(context, iconSize, iconLabelSpacing, maxWidth),
          iconSize.pixels,
        )
      heights.add(height)
      buildLines(context, maxWidth) { item ->
        val currentHeight =
          max(item.getLabelHeight(context, iconSize, iconLabelSpacing, maxWidth), iconSize.pixels)
        heights.add(currentHeight)
        height += currentHeight
      }
      height + (lines.size - 1) * rowSpacing.pixels + padding.vertical.pixels
    }

  override fun draw(context: D) {
    with(context) {
      var currentTop = bounds.top + padding.top.pixels
      // isLtr? startX means the line starts at X from left : it starts at X from right
      val startX =
        if (isLtr) {
          bounds.left + padding.start.pixels
        } else {
          bounds.right - padding.start.pixels - iconSize.pixels
        }
      val availableWidth = bounds.width

      lines.forEachIndexed { index, item ->
        var currentStart = 0f
        val currentLineHeight =
          heights.getOrElse(index) {
            item.first().getLabelHeight(context, iconSize, iconLabelSpacing, availableWidth)
          }
        val centerY = currentTop + currentLineHeight.half

        item.forEach {
          it.icon.draw(
            context = context,
            left = startX + currentStart,
            top = centerY - iconSize.pixels.half,
            right = startX + iconSize.pixels + currentStart,
            bottom = centerY + iconSize.pixels.half,
          )
          currentStart +=
            if (isLtr) {
              (iconSize + iconLabelSpacing).pixels
            } else {
              -iconLabelSpacing.pixels
            }
          it.labelComponent.draw(
            context = context,
            text = it.label,
            x = startX + currentStart,
            y = centerY,
            horizontalPosition = Position.Horizontal.End,
            verticalPosition = Position.Vertical.Center,
            maxWidth =
              (bounds.width - (iconSize + iconLabelSpacing + padding.horizontal).pixels).toInt(),
          )
          currentStart +=
            if (isLtr) {
              it.getLabelWidth(context, iconSize, iconLabelSpacing, availableWidth) +
                columnSpacing.pixels
            } else {
              -(it.getLabelWidth(context, iconSize, iconLabelSpacing, availableWidth) +
                columnSpacing.pixels +
                iconSize.pixels)
            }
        }
        currentTop += currentLineHeight + rowSpacing.pixels
      }
    }
  }

  protected fun buildLines(
    context: MeasuringContext,
    availableWidth: Float,
    callback: (it: LegendItem) -> Unit = {},
  ): Unit =
    with(context) {
      var remainWidth = availableWidth
      var currentLine = 0
      lines.clear()
      lines.add(mutableListOf())
      itemManager.itemList.forEach {
        remainWidth -=
          it.getWidth(context, iconSize, iconLabelSpacing, availableWidth) + columnSpacing.pixels
        if (remainWidth >= 0 || remainWidth == availableWidth) {
          lines[currentLine].add(it)
          return@forEach
        }

        currentLine++
        remainWidth =
          availableWidth -
            it.getWidth(context, iconSize, iconLabelSpacing, availableWidth) -
            columnSpacing.pixels
        lines.add(mutableListOf(it))
        callback(it)
      }
    }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is HorizontalLegend<*, *> &&
        items == other.items &&
        iconSize == iconSize &&
        iconLabelSpacing == iconLabelSpacing &&
        rowSpacing == other.rowSpacing &&
        columnSpacing == other.columnSpacing &&
        padding == other.padding

  override fun hashCode(): Int {
    var result = items.hashCode()
    result = 31 * result + iconSize.hashCode()
    result = 31 * result + iconLabelSpacing.hashCode()
    result = 31 * result + rowSpacing.hashCode()
    result = 31 * result + columnSpacing.hashCode()
    result = 31 * result + padding.hashCode()
    return result
  }
}
