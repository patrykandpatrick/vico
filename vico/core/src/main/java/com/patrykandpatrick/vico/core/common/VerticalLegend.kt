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
import kotlin.math.max

/**
 * [VerticalLegend] displays legend items in a vertical list.
 *
 * @param items a [Collection] of [LegendItem]s to be displayed by this [VerticalLegend].
 * @param iconSizeDp defines the size of all [LegendItem.icon]s.
 * @param iconPaddingDp defines the padding between each [LegendItem.icon] and its corresponding
 *   [LegendItem.label].
 * @param spacingDp defines the vertical spacing between each [LegendItem].
 * @param padding defines the padding of the content.
 */
public open class VerticalLegend<M : MeasureContext, D : DrawContext>(
  protected val items: Collection<LegendItem>,
  protected val iconSizeDp: Float,
  protected val iconPaddingDp: Float,
  protected val spacingDp: Float = 0f,
  protected val padding: Dimensions = Dimensions.Empty,
) : Legend<M, D> {
  private val heights: HashMap<LegendItem, Float> = HashMap()

  override val bounds: RectF = RectF()

  override fun getHeight(context: M, maxWidth: Float): Float =
    with(context) {
      items.fold(0f) { sum, item ->
        sum +
          max(iconSizeDp.pixels, item.getLabelHeight(context, maxWidth, iconPaddingDp, iconSizeDp))
            .also { height -> heights[item] = height }
      } + (padding.verticalDp + spacingDp * (items.size - 1)).pixels
    }

  override fun draw(context: D) {
    with(context) {
      var currentTop = bounds.top + padding.topDp.pixels

      items.forEach { item ->
        val height =
          heights.getOrPut(item) {
            item.getLabelHeight(this, bounds.width(), iconPaddingDp, iconSizeDp)
          }
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
            (iconSizeDp + iconPaddingDp).pixels
          } else {
            -iconPaddingDp.pixels
          }

        item.labelComponent.draw(
          context = context,
          text = item.label,
          x = startX,
          y = centerY,
          horizontalPosition = HorizontalPosition.End,
          maxWidth =
            (bounds.width() - (iconSizeDp + iconPaddingDp + padding.horizontalDp).pixels).toInt(),
        )

        currentTop += height + spacingDp.pixels
      }
    }
  }
}
