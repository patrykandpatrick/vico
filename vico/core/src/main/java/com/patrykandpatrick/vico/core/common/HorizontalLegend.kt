/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.common

import android.graphics.RectF
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlin.math.max

/**
 * [HorizontalLegend] displays legend items beside one another in lines.
 *
 * @property items adds the [LegendItem]s.
 * @property iconSizeDp the [LegendItem.icon] size (in dp).
 * @property iconLabelSpacingDp the spacing between [LegendItem.icon] and
 *   [LegendItem.labelComponent] (in dp).
 * @property rowSpacingDp the row spacing (in dp).
 * @property columnSpacingDp the column spacing (in dp).
 * @property padding the content padding.
 */
public open class HorizontalLegend<M : MeasuringContext, D : DrawingContext>(
  protected val items: AdditionScope<LegendItem>.(ExtraStore) -> Unit,
  protected val iconSizeDp: Float = Defaults.LEGEND_ICON_SIZE,
  protected val iconLabelSpacingDp: Float = Defaults.LEGEND_ICON_LABEL_SPACING,
  protected val rowSpacingDp: Float = Defaults.LEGEND_ROW_SPACING,
  protected val columnSpacingDp: Float = Defaults.LEGEND_COLUMN_SPACING,
  protected val padding: Dimensions = Dimensions.Empty,
) : Legend<M, D> {
  private val itemManager = LegendItemManager(items)
  private val heights = mutableListOf<Float>()
  private val lines = mutableListOf<MutableList<LegendItem>>(mutableListOf())
  override val bounds: RectF = RectF()

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
            .getLabelHeight(context, maxWidth, iconLabelSpacingDp, iconSizeDp),
          iconSizeDp.pixels,
        )
      heights.add(height)
      buildLines(context, maxWidth) { item ->
        val currentHeight =
          max(
            item.getLabelHeight(context, maxWidth, iconLabelSpacingDp, iconSizeDp),
            iconSizeDp.pixels,
          )
        heights.add(currentHeight)
        height += currentHeight
      }
      height + (lines.size - 1) * rowSpacingDp.pixels + padding.verticalDp.pixels
    }

  override fun draw(context: D) {
    with(context) {
      var currentTop = bounds.top + padding.topDp.pixels
      // isLtr? startX means the line starts at X from left : it starts at X from right
      val startX =
        if (isLtr) {
          bounds.left + padding.startDp.pixels
        } else {
          bounds.right - padding.startDp.pixels - iconSizeDp.pixels
        }
      val availableWidth = bounds.width()

      lines.forEachIndexed { index, item ->
        var currentStart = 0f
        val currentLineHeight =
          heights.getOrElse(index) {
            item.first().getLabelHeight(context, availableWidth, iconLabelSpacingDp, iconSizeDp)
          }
        val centerY = currentTop + currentLineHeight.half

        item.forEach {
          it.icon.draw(
            context = context,
            left = startX + currentStart,
            top = centerY - iconSizeDp.half.pixels,
            right = startX + iconSizeDp.pixels + currentStart,
            bottom = centerY + iconSizeDp.half.pixels,
          )
          currentStart +=
            if (isLtr) {
              (iconSizeDp + iconLabelSpacingDp).pixels
            } else {
              -iconLabelSpacingDp.pixels
            }
          it.labelComponent.draw(
            context = context,
            text = it.label,
            x = startX + currentStart,
            y = centerY,
            horizontalPosition = HorizontalPosition.End,
            verticalPosition = VerticalPosition.Center,
            maxWidth =
              (bounds.width() - (iconSizeDp + iconLabelSpacingDp + padding.horizontalDp).pixels)
                .toInt(),
          )
          currentStart +=
            if (isLtr) {
              it.getLabelWidth(context, availableWidth, iconLabelSpacingDp, iconSizeDp) +
                columnSpacingDp.pixels
            } else {
              -(it.getLabelWidth(context, availableWidth, iconLabelSpacingDp, iconSizeDp) +
                columnSpacingDp.pixels +
                iconSizeDp.pixels)
            }
        }
        currentTop += currentLineHeight + rowSpacingDp.pixels
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
          it.getWidth(context, availableWidth, iconLabelSpacingDp, iconSizeDp) +
            columnSpacingDp.pixels
        if (remainWidth >= 0 || remainWidth == availableWidth) {
          lines[currentLine].add(it)
          return@forEach
        }

        currentLine++
        remainWidth =
          availableWidth -
            it.getWidth(context, availableWidth, iconLabelSpacingDp, iconSizeDp) -
            columnSpacingDp.pixels
        lines.add(mutableListOf(it))
        callback(it)
      }
    }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is HorizontalLegend<*, *> &&
        items == other.items &&
        iconSizeDp == iconSizeDp &&
        iconLabelSpacingDp == iconLabelSpacingDp &&
        rowSpacingDp == other.rowSpacingDp &&
        columnSpacingDp == other.columnSpacingDp &&
        padding == other.padding

  override fun hashCode(): Int {
    var result = items.hashCode()
    result = 31 * result + iconSizeDp.hashCode()
    result = 31 * result + iconLabelSpacingDp.hashCode()
    result = 31 * result + rowSpacingDp.hashCode()
    result = 31 * result + columnSpacingDp.hashCode()
    result = 31 * result + padding.hashCode()
    return result
  }
}
