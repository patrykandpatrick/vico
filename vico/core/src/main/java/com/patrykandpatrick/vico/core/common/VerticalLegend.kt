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
 * [VerticalLegend] displays legend items in a vertical list.
 *
 * @param items adds the [LegendItem]s.
 * @param iconSizeDp the [LegendItem.icon] size (in dp).
 * @param iconLabelSpacingDp the spacing between [LegendItem.icon] and [LegendItem.label] (in dp).
 * @param rowSpacingDp the row spacing (in dp).
 * @param padding the content padding.
 */
public open class VerticalLegend<M : MeasuringContext, D : DrawingContext>(
  protected val items: AdditionScope<LegendItem>.(ExtraStore) -> Unit,
  protected val iconSizeDp: Float = Defaults.LEGEND_ICON_SIZE,
  protected val iconLabelSpacingDp: Float = Defaults.LEGEND_ICON_LABEL_SPACING,
  protected val rowSpacingDp: Float = Defaults.LEGEND_ROW_SPACING,
  protected val padding: Insets = Insets.Zero,
) : Legend<M, D> {
  private val itemManager = LegendItemManager(items)
  private val heights: HashMap<LegendItem, Float> = HashMap()

  override val bounds: RectF = RectF()

  override fun getHeight(context: M, maxWidth: Float): Float =
    with(context) {
      itemManager.addItems(this)
      itemManager.itemList.fold(0f) { sum, item ->
        sum +
          max(
              iconSizeDp.pixels,
              item.getLabelHeight(context, iconSizeDp, iconLabelSpacingDp, maxWidth),
            )
            .also { height -> heights[item] = height }
      } + (padding.verticalDp + rowSpacingDp * (itemManager.itemList.size - 1)).pixels
    }

  override fun draw(context: D) {
    with(context) {
      var currentTop = bounds.top + padding.topDp.pixels

      itemManager.itemList.forEach { item ->
        val height = heights.getValue(item)
        val centerY = currentTop + height.half
        var startX =
          if (isLtr) {
            bounds.left + padding.startDp.pixels
          } else {
            bounds.right - padding.startDp.pixels - iconSizeDp.pixels
          }

        item.icon.draw(
          context = context,
          left = startX,
          top = centerY - iconSizeDp.half.pixels,
          right = startX + iconSizeDp.pixels,
          bottom = centerY + iconSizeDp.half.pixels,
        )

        startX +=
          if (isLtr) {
            (iconSizeDp + iconLabelSpacingDp).pixels
          } else {
            -iconLabelSpacingDp.pixels
          }

        item.labelComponent.draw(
          context = context,
          text = item.label,
          x = startX,
          y = centerY,
          horizontalPosition = Position.Horizontal.End,
          maxWidth =
            (bounds.width() - (iconSizeDp + iconLabelSpacingDp + padding.horizontalDp).pixels)
              .toInt(),
        )

        currentTop += height + rowSpacingDp.pixels
      }
    }
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is VerticalLegend<*, *> &&
        items == other.items &&
        iconSizeDp == other.iconSizeDp &&
        iconLabelSpacingDp == other.iconLabelSpacingDp &&
        rowSpacingDp == other.rowSpacingDp &&
        padding == other.padding

  override fun hashCode(): Int {
    var result = items.hashCode()
    result = 31 * result + iconSizeDp.hashCode()
    result = 31 * result + iconLabelSpacingDp.hashCode()
    result = 31 * result + rowSpacingDp.hashCode()
    result = 31 * result + padding.hashCode()
    return result
  }
}
