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

/**
 * [HorizontalLegend] displays legend items beside one another in lines.
 *
 * @param items a [Collection] of [LegendItem]s to be displayed by this [HorizontalLegend].
 * @param iconSizeDp defines the size of all [LegendItem.icon]s.
 * @param iconPaddingDp defines the padding between each [LegendItem.icon] and its corresponding [LegendItem.label].
 * @param lineSpacingDp defines the spacing between adjacent lines.
 * @param spacingDp defines the horizontal spacing between adjacent [LegendItem]s.
 * @param padding defines the padding of the content.
 */
public open class HorizontalLegend<M : MeasureContext, D : DrawContext>(
    public var items: Collection<LegendItem>,
    public var iconSizeDp: Float,
    public var iconPaddingDp: Float,
    public var lineSpacingDp: Float = 0f,
    public var spacingDp: Float = 0f,
    public val padding: Dimensions = Dimensions.Empty,
) : Legend<M, D> {
    private val heights = mutableListOf<Float>()

    private val lines = mutableListOf<MutableList<LegendItem>>(mutableListOf())

    override val bounds: RectF = RectF()

    override fun getHeight(
        context: M,
        availableWidth: Float,
    ): Float =
        with(context) {
            if (items.isEmpty()) return@with 0f
            lines.clear()
            lines.add(mutableListOf())
            var height =
                maxOf(
                    items.first().getLabelHeight(context, availableWidth, iconPaddingDp, iconSizeDp),
                    iconSizeDp.pixels,
                )
            heights.add(height)
            buildLines(context, availableWidth) {
                val currentHeight =
                    maxOf(it.getLabelHeight(context, availableWidth, iconPaddingDp, iconSizeDp), iconSizeDp.pixels)
                heights.add(currentHeight)
                height += currentHeight
            }
            height + (lines.size - 1) * lineSpacingDp.pixels + padding.verticalDp.pixels
        }

    override fun draw(
        context: D,
        chartBounds: RectF,
    ): Unit =
        with(context) {
            var currentTop = bounds.top + padding.topDp.pixels
            // isLtr? startX means the line starts at X from left : it starts at X from right
            val startX =
                if (isLtr) {
                    chartBounds.left + padding.startDp.pixels
                } else {
                    chartBounds.right - padding.startDp.pixels - iconSizeDp.pixels
                }
            val availableWidth = chartBounds.width()
            if (lines.isEmpty()) {
                buildLines(context, availableWidth)
            }

            lines.forEachIndexed { index, item ->
                var currentStart = 0f
                val currentLineHeight =
                    heights.getOrElse(index) {
                        item.first().getLabelHeight(context, availableWidth, iconPaddingDp, iconSizeDp)
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
                            (iconSizeDp + iconPaddingDp).pixels
                        } else {
                            -iconPaddingDp.pixels
                        }
                    it.label.drawText(
                        context = context,
                        text = it.labelText,
                        textX = startX + currentStart,
                        textY = centerY,
                        horizontalPosition = HorizontalPosition.End,
                        verticalPosition = VerticalPosition.Center,
                        maxTextWidth =
                            (chartBounds.width() - (iconSizeDp + iconPaddingDp + padding.horizontalDp).pixels).toInt(),
                    )
                    currentStart +=
                        if (isLtr) {
                            it.getLabelWidth(context, availableWidth, iconPaddingDp, iconSizeDp) + spacingDp.pixels
                        } else {
                            -(
                                it.getLabelWidth(
                                    context,
                                    availableWidth,
                                    iconPaddingDp,
                                    iconSizeDp,
                                ) + spacingDp.pixels + iconSizeDp.pixels
                            )
                        }
                }
                currentTop += currentLineHeight + lineSpacingDp.pixels
            }
        }

    protected fun buildLines(
        context: MeasureContext,
        availableWidth: Float,
        callback: (it: LegendItem) -> Unit = {},
    ): Unit =
        with(context) {
            var remainWidth = availableWidth
            var currentLine = 0
            lines.clear()
            lines.add(mutableListOf())
            items.forEach {
                remainWidth -= it.getWidth(
                    context,
                    availableWidth,
                    iconPaddingDp,
                    iconSizeDp,
                ) + spacingDp.pixels
                if (remainWidth >= 0 || remainWidth == availableWidth) {
                    lines[currentLine].add(it)
                    return@forEach
                }

                currentLine++
                remainWidth = availableWidth -
                    it.getWidth(
                        context,
                        availableWidth,
                        iconPaddingDp,
                        iconSizeDp,
                    ) - spacingDp.pixels
                lines.add(mutableListOf(it))
                callback(it)
            }
        }
}
