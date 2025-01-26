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
 * [VerticalLegend] displays legend items in a vertical list.
 *
 * @param items adds the [LegendItem]s.
 * @param iconSize the [LegendItem.icon] size.
 * @param iconLabelSpacing the spacing between [LegendItem.icon] and [LegendItem.label].
 * @param rowSpacing the row spacing.
 * @param padding the content padding.
 */
public open class VerticalLegend<M : MeasuringContext, D : DrawingContext>(
  protected val items: AdditionScope<LegendItem>.(ExtraStore) -> Unit,
  protected val iconSize: Dp = Defaults.LEGEND_ICON_SIZE.dp,
  protected val iconLabelSpacing: Dp = Defaults.LEGEND_ICON_LABEL_SPACING.dp,
  protected val rowSpacing: Dp = Defaults.LEGEND_ROW_SPACING.dp,
  protected val padding: Insets = Insets.Zero,
) : Legend<M, D> {
  private val itemManager = LegendItemManager(items)
  private val heights: HashMap<LegendItem, Float> = HashMap()

  override var bounds: Rect = Rect.Zero

  override fun getHeight(context: M, maxWidth: Float): Float =
    with(context) {
      itemManager.addItems(this)
      itemManager.itemList.fold(0f) { sum, item ->
        sum +
          max(iconSize.pixels, item.getLabelHeight(context, iconSize, iconLabelSpacing, maxWidth))
            .also { height -> heights[item] = height }
      } + (padding.vertical + rowSpacing * (itemManager.itemList.size - 1)).pixels
    }

  override fun draw(context: D) {
    with(context) {
      var currentTop = bounds.top + padding.top.pixels

      itemManager.itemList.forEach { item ->
        val height = heights.getValue(item)
        val centerY = currentTop + height.half
        var startX =
          if (isLtr) {
            bounds.left + padding.start.pixels
          } else {
            bounds.right - padding.start.pixels - iconSize.pixels
          }

        item.icon.draw(
          context = context,
          left = startX,
          top = centerY - iconSize.pixels.half,
          right = startX + iconSize.pixels,
          bottom = centerY + iconSize.pixels.half,
        )

        startX +=
          if (isLtr) {
            (iconSize + iconLabelSpacing).pixels
          } else {
            -iconLabelSpacing.pixels
          }

        item.labelComponent.draw(
          context = context,
          text = item.label,
          x = startX,
          y = centerY,
          horizontalPosition = Position.Horizontal.End,
          maxWidth =
            (bounds.width - (iconSize + iconLabelSpacing + padding.horizontal).pixels).toInt(),
        )

        currentTop += height + rowSpacing.pixels
      }
    }
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is VerticalLegend<*, *> &&
        items == other.items &&
        iconSize == other.iconSize &&
        iconLabelSpacing == other.iconLabelSpacing &&
        rowSpacing == other.rowSpacing &&
        padding == other.padding

  override fun hashCode(): Int {
    var result = items.hashCode()
    result = 31 * result + iconSize.hashCode()
    result = 31 * result + iconLabelSpacing.hashCode()
    result = 31 * result + rowSpacing.hashCode()
    result = 31 * result + padding.hashCode()
    return result
  }
}
