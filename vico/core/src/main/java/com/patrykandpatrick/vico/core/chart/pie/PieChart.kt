/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.chart.pie

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.DefaultDimens.PIE_CHART_START_ANGLE
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.constants.FULL_DEGREES
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.dimensions.BoundsAware
import com.patrykandpatrick.vico.core.entry.PieEntryModel
import com.patrykandpatrick.vico.core.extension.getRepeating
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.ifNotNull
import com.patrykandpatrick.vico.core.extension.maxOfOrNullIndexed
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.extension.round
import com.patrykandpatrick.vico.core.extension.set

/**
 * TODO
 * @param slices TODO
 * @param spacingDp TODO
 * @param startAngle TODO
 * @param holeRadiusDp TODO
 */
public open class PieChart(
    public val slices: List<Slice>,
    public var spacingDp: Float = 0f,
    public var holeRadiusDp: Float = 0f,
    public var startAngle: Float = PIE_CHART_START_ANGLE,
) : BoundsAware {

    init {
        require(slices.isNotEmpty()) { "Slices cannot be empty." }
        require(spacingDp >= 0f) { "The spacing cannot be negative." }
        require(holeRadiusDp >= 0f) { "The hole radius cannot be negative." }
    }

    override val bounds: RectF = RectF()

    /**
     * TODO
     */
    protected val oval: RectF = RectF()

    /**
     * TODO
     */
    protected val spacingPathBuilder: Path = Path()

    /**
     * TODO
     */
    protected val spacingMatrix: Matrix = Matrix()

    protected val insets: Insets = Insets()

    /**
     * TODO
     */
    public open fun updateOvalBounds(
        context: DrawContext,
        model: PieEntryModel,
    ): Unit = with(context) {

        insets.clear()

        var ovalRadius = bounds.width().coerceAtMost(bounds.height()).half

        var startAngle = startAngle

        val maxOffsetFromCenter = model.entries.maxOfOrNullIndexed { index, entry ->
            val slice = slices.getRepeating(index)

            val sweepAngle = entry.value / model.maxValue * FULL_DEGREES

            ifNotNull(slice.label, entry.label) { labelComponent, label ->

                oval.set(
                    left = bounds.centerX() - ovalRadius,
                    top = bounds.centerY() - ovalRadius,
                    right = bounds.centerX() + ovalRadius,
                    bottom = bounds.centerY() + ovalRadius,
                )

                labelComponent.getInsets(
                    context = context,
                    contentBounds = bounds,
                    oval = oval,
                    label = label,
                    outInsets = insets,
                    angle = startAngle + sweepAngle.half,
                )
            }

            startAngle += sweepAngle

            slice.offsetFromCenterDp.pixels
        }.orZero

        ovalRadius -= maxOffsetFromCenter + insets.largestEdge
        ovalRadius = ovalRadius.round

        oval.set(
            left = bounds.centerX() - ovalRadius,
            top = bounds.centerY() - ovalRadius,
            right = bounds.centerX() + ovalRadius,
            bottom = bounds.centerY() + ovalRadius,
        )
    }

    /**
     * TODO
     */
    public fun draw(
        context: DrawContext,
        model: PieEntryModel,
    ): Unit = with(context) {

        updateOvalBounds(context, model)

        val restoreCount = if (spacingDp > 0f) saveLayer() else -1

        model.entries.foldIndexed(startAngle) { index, startAngle, entry ->

            val slice = slices.getRepeating(index)

            val sweepAngle = entry.value / model.maxValue * FULL_DEGREES

            spacingPathBuilder.rewind()

            if (spacingDp > 0f) {
                addSpacingSegment(spacingPathBuilder, sweepAngle)
                addSpacingSegment(spacingPathBuilder, startAngle)
                addHole(spacingPathBuilder, slice.offsetFromCenterDp.pixels)
            }

            slice.draw(
                context = context,
                contentBounds = bounds,
                oval = oval,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                holeRadius = holeRadiusDp.pixels,
                label = entry.label,
                spacingPath = spacingPathBuilder,
            )

            startAngle + sweepAngle
        }

        if (restoreCount >= 0) restoreCanvasToCount(restoreCount)
    }

    protected open fun DrawContext.addSpacingSegment(
        pathBuilder: Path,
        angle: Float,
    ) {
        val spacing = spacingDp.pixels
        with(pathBuilder) {
            spacingMatrix.postRotate(angle, oval.centerX(), oval.centerY())
            moveTo(oval.centerX(), oval.centerY() + spacing.half)
            lineTo(oval.right + spacing, oval.centerY() + spacing.half)
            lineTo(oval.right + spacing, oval.centerY() - spacing.half)
            lineTo(oval.centerX(), oval.centerY() - spacing.half)
            close()
            transform(spacingMatrix)
            spacingMatrix.reset()
        }
    }

    protected open fun DrawContext.addHole(
        pathBuilder: Path,
        offsetFromCenter: Float,
    ): Unit = with(pathBuilder) {
        addCircle(oval.centerX(), oval.centerY(), holeRadiusDp.pixels, Path.Direction.CCW)
    }
}
