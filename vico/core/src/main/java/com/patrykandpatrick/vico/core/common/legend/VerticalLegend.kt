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

package com.patrykandpatrick.vico.core.common.legend

import android.graphics.RectF
import com.patrykandpatrick.vico.core.common.DrawContext
import com.patrykandpatrick.vico.core.common.HorizontalPosition
import com.patrykandpatrick.vico.core.common.MeasureContext
import com.patrykandpatrick.vico.core.common.MutableDimensions
import com.patrykandpatrick.vico.core.common.Padding
import com.patrykandpatrick.vico.core.common.extension.half

/**
 * [VerticalLegend] displays legend items in a vertical list.
 *
 * @param items a [Collection] of [LegendItem]s to be displayed by this [VerticalLegend].
 * @param iconSizeDp defines the size of all [LegendItem.icon]s.
 * @param iconPaddingDp defines the padding between each [LegendItem.icon] and its corresponding [LegendItem.label].
 * @param spacingDp defines the vertical spacing between each [LegendItem].
 * @param padding defines the padding of the content.
 */
public open class VerticalLegend(
    public var items: Collection<LegendItem>,
    public var iconSizeDp: Float,
    public var iconPaddingDp: Float,
    public var spacingDp: Float = 0f,
    override val padding: MutableDimensions = MutableDimensions.empty(),
) : Legend, Padding {
    private val heights: HashMap<LegendItem, Float> = HashMap()

    override val bounds: RectF = RectF()

    override fun getHeight(
        context: MeasureContext,
        availableWidth: Float,
    ): Float =
        with(context) {
            items.fold(0f) { sum, item ->
                sum +
                    maxOf(
                        iconSizeDp.pixels,
                        item.getLabelHeight(context, availableWidth, iconPaddingDp, iconSizeDp),
                    ).also { height -> heights[item] = height }
            } + (padding.verticalDp + spacingDp * (items.size - 1)).pixels
        }

    override fun draw(
        context: DrawContext,
        chartBounds: RectF,
    ): Unit =
        with(context) {
            var currentTop = bounds.top + padding.topDp.pixels

            items.forEach { item ->

                val height =
                    heights.getOrPut(item) {
                        item.getLabelHeight(this, chartBounds.width(), iconPaddingDp, iconSizeDp)
                    }
                val centerY = currentTop + height.half
                var startX =
                    if (isLtr) {
                        chartBounds.left + padding.startDp.pixels
                    } else {
                        chartBounds.right - padding.startDp.pixels - iconSizeDp.pixels
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

                item.label.drawText(
                    context = context,
                    text = item.labelText,
                    textX = startX,
                    textY = centerY,
                    horizontalPosition = HorizontalPosition.End,
                    maxTextWidth =
                        (chartBounds.width() - (iconSizeDp + iconPaddingDp + padding.horizontalDp).pixels)
                            .toInt(),
                )

                currentTop += height + spacingDp.pixels
            }
        }
}
