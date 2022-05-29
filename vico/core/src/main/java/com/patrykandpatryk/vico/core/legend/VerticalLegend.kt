/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.core.legend

import android.graphics.RectF
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.component.Component
import com.patrykandpatryk.vico.core.component.dimension.Padding
import com.patrykandpatryk.vico.core.component.text.HorizontalPosition
import com.patrykandpatryk.vico.core.component.text.TextComponent
import com.patrykandpatryk.vico.core.dimensions.MutableDimensions
import com.patrykandpatryk.vico.core.dimensions.emptyDimensions
import com.patrykandpatryk.vico.core.extension.half

/**
 * [VerticalLegend] displays legend items in a vertical list.
 *
 * @param items a [Collection] of [Item]s to be displayed by this [VerticalLegend].
 * @param iconSizeDp defines the size of all [Item.icon]s.
 * @param iconPaddingDp defines the padding between each [Item.icon] and its corresponding [Item.label].
 * @param spacingDp defines the vertical spacing between each [Item].
 * @param padding defines the padding of the content.
 */
public open class VerticalLegend(
    public var items: Collection<Item>,
    public var iconSizeDp: Float,
    public var iconPaddingDp: Float,
    public var spacingDp: Float = 0f,
    override val padding: MutableDimensions = emptyDimensions(),
) : Legend, Padding {

    private val heights: HashMap<Item, Float> = HashMap()

    override val bounds: RectF = RectF()

    override fun getHeight(context: ChartDrawContext, availableWidth: Float): Float = with(context) {
        items.fold(0f) { sum, item ->
            sum + maxOf(
                iconSizeDp.pixels,
                item.getHeight(context, availableWidth),
            ).also { height -> heights[item] = height }
        } + (padding.verticalDp + spacingDp * (items.size - 1)).pixels
    }

    override fun draw(context: ChartDrawContext): Unit = with(context) {

        var currentTop = bounds.top + padding.topDp.pixels

        items.forEach { item ->

            val height = heights.getOrPut(item) { item.getHeight(this, chartBounds.width()) }
            val centerY = currentTop + height.half
            var startX = if (isLtr) {
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

            startX += if (isLtr) {
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
                maxTextWidth = (chartBounds.width() - (iconSizeDp + iconPaddingDp + padding.horizontalDp).pixels)
                    .toInt()
            )

            currentTop += height + spacingDp.pixels
        }
    }

    protected open fun Item.getHeight(
        context: ChartDrawContext,
        availableWidth: Float,
    ): Float = with(context) {
        label.getHeight(
            context = context,
            text = labelText,
            width = (availableWidth - iconSizeDp.pixels - iconPaddingDp.pixels).toInt(),
        )
    }

    /**
     * Defines the appearance of an item of a [Legend].
     *
     * @param icon the [Component] used as the item’s icon.
     * @param label the [TextComponent] used for the label.
     * @param labelText the text content of the label.
     */
    public class Item(
        public val icon: Component,
        public val label: TextComponent,
        public val labelText: CharSequence,
    )
}
