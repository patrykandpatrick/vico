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

package com.patrykandpatrick.vico.core.chart

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.patrykandpatrick.vico.core.DefaultDimens.PIE_CHART_START_ANGLE
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.dimensions.BoundsAware
import com.patrykandpatrick.vico.core.entry.PieEntryModel
import com.patrykandpatrick.vico.core.extension.PI_RAD
import com.patrykandpatrick.vico.core.extension.getRepeating
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.isNotTransparent
import com.patrykandpatrick.vico.core.extension.maxOfOrNullIndexed
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.core.extension.updateBy
import kotlin.math.cos
import kotlin.math.sin

private const val FULL_DEGREES = 360f

public open class PieChart(
    public var startAngle: Float = PIE_CHART_START_ANGLE,
    protected val slices: List<Slice>,
) : BoundsAware {

    init {
        require(slices.isNotEmpty()) { "Slices cannot be empty." }
    }

    public open class Slice(
        public var color: Int = Color.LTGRAY,
        public var dynamicShader: DynamicShader? = null,
        public var strokeWidthDp: Float = 0f,
        public var strokeColor: Int = Color.TRANSPARENT,
        public var offsetFromCenterDp: Float = 0f,
    ) {

        protected val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
        }

        protected val drawOval: RectF = RectF()

        public open fun draw(
            context: DrawContext,
            oval: RectF,
            startAngle: Float,
            sweepAngle: Float,
        ): Unit = with(context) {

            val strokeWidth = strokeWidthDp.pixels

            drawOval.set(oval)
            applyOffset(context, drawOval, startAngle + sweepAngle.half)

            if (color.isNotTransparent) {

                paint.style = Paint.Style.FILL
                paint.color = color

                canvas.drawArc(
                    drawOval,
                    startAngle,
                    sweepAngle,
                    true,
                    paint,
                )
            }

            if (strokeColor.isNotTransparent && strokeWidthDp > 0f) {

                paint.style = Paint.Style.STROKE
                paint.color = strokeColor
                paint.strokeWidth = strokeWidth

                drawOval.updateBy(
                    left = strokeWidth.half,
                    top = strokeWidth.half,
                    right = -strokeWidth.half,
                    bottom = -strokeWidth.half,
                )

                canvas.drawArc(
                    drawOval,
                    startAngle,
                    sweepAngle,
                    true,
                    paint,
                )
            }
        }

        public fun applyOffset(context: MeasureContext, rectF: RectF, angle: Float): Unit = with(context) {
            val xOffset = (offsetFromCenterDp.pixels * cos(angle * Math.PI / PI_RAD)).toFloat()
            val yOffset = (offsetFromCenterDp.pixels * sin(angle * Math.PI / PI_RAD)).toFloat()

            rectF.offset(xOffset, yOffset)
        }
    }

    override val bounds: RectF = RectF()

    protected val oval: RectF = RectF()

    public open fun updateOvalBounds(
        context: MeasureContext,
        model: PieEntryModel,
    ): Unit = with(context) {

        val maxOffsetFromCenter = model.entries.maxOfOrNullIndexed { index, _ ->
            slices.getRepeating(index).offsetFromCenterDp.pixels
        }.orZero

        val ovalRadius = bounds.width().coerceAtMost(bounds.height()).half - maxOffsetFromCenter

        oval.set(
            left = bounds.centerX() - ovalRadius,
            top = bounds.centerY() - ovalRadius,
            right = bounds.centerX() + ovalRadius,
            bottom = bounds.centerY() + ovalRadius,
        )
    }

    public fun draw(
        context: DrawContext,
        model: PieEntryModel,
    ): Unit = with(context) {

        updateOvalBounds(context, model)

        model.entries.foldIndexed(startAngle) { index, startAngle, entry ->

            val slice = slices.getRepeating(index)

            val sweepAngle = entry.value / model.maxValue * FULL_DEGREES

            slice.draw(
                context = context,
                oval = oval,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
            )

            startAngle + sweepAngle
        }
    }
}
