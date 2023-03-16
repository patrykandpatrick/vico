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
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import com.patrykandpatrick.vico.core.DefaultDimens.PIE_CHART_START_ANGLE
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.dimensions.BoundsAware
import com.patrykandpatrick.vico.core.entry.PieEntryModel
import com.patrykandpatrick.vico.core.extension.getRepeating
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.isNotTransparent
import com.patrykandpatrick.vico.core.extension.maxOfOrNullIndexed
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.core.extension.updateBy
import com.patrykandpatrick.vico.core.layout.PieLayoutHelper
import com.patrykandpatrick.vico.core.math.translateXByAngle
import com.patrykandpatrick.vico.core.math.translateYByAngle

private const val FULL_DEGREES = 360f

public open class PieChart(
    public val slices: List<Slice>,
    public var spacingDp: Float = 0f,
    public var startAngle: Float = PIE_CHART_START_ANGLE,
) : BoundsAware {

    init {
        require(slices.isNotEmpty()) { "Slices cannot be empty." }
        require(spacingDp >= 0f) { "The spacing cannot be negative." }
    }

    public open class Slice(
        public var color: Int = Color.LTGRAY,
        public var dynamicShader: DynamicShader? = null,
        public var strokeWidthDp: Float = 0f,
        public var strokeColor: Int = Color.TRANSPARENT,
        public var offsetFromCenterDp: Float = 0f,
        public var textComponent: TextComponent = textComponent(),
    ) {

        protected val layoutHelper: PieLayoutHelper = PieLayoutHelper()

        protected val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
        }

        protected val drawOval: RectF = RectF()

        protected val slicePath: Path = Path()

        public open fun draw(
            context: DrawContext,
            oval: RectF,
            startAngle: Float,
            sweepAngle: Float,
            label: CharSequence?,
        ): Unit = with(context) {

            drawOval.set(oval)
            applyOffset(drawOval, startAngle + sweepAngle.half)

            if (color.isNotTransparent) {
                drawFilledSlice(context, startAngle, sweepAngle)
            }

            if (strokeColor.isNotTransparent && strokeWidthDp > 0f) {
                drawStrokedSlice(context, startAngle, sweepAngle)
            }

            if (label != null) {
                drawLabel(context, drawOval, startAngle, sweepAngle, label)
            }
        }

        protected open fun drawFilledSlice(
            context: DrawContext,
            startAngle: Float,
            sweepAngle: Float,
        ): Unit = with(context) {

            paint.style = Paint.Style.FILL
            paint.color = color

            slicePath.rewind()

            slicePath.addArc(drawOval, startAngle, sweepAngle)

            slicePath.lineTo(drawOval.centerX(), drawOval.centerY())

            slicePath.close()

            canvas.drawPath(slicePath, paint)
        }

        protected open fun drawStrokedSlice(
            context: DrawContext,
            startAngle: Float,
            sweepAngle: Float,
        ): Unit = with(context) {
            val strokeWidth = strokeWidthDp.pixels

            paint.style = Paint.Style.STROKE
            paint.color = strokeColor
            paint.strokeWidth = strokeWidth

            drawOval.updateBy(
                left = strokeWidth.half,
                top = strokeWidth.half,
                right = -strokeWidth.half,
                bottom = -strokeWidth.half,
            )

            slicePath.rewind()

            slicePath.addArc(drawOval, startAngle, sweepAngle)

            slicePath.lineTo(drawOval.centerX(), drawOval.centerY())

            slicePath.close()

            canvas.drawPath(slicePath, paint)
        }

        protected open fun drawLabel(
            context: DrawContext,
            drawOval: RectF,
            startAngle: Float,
            sweepAngle: Float,
            label: CharSequence,
        ): Unit = with(context) {

            val radius = drawOval.width().half

            val textX = drawOval.centerX() + radius.half.translateXByAngle(startAngle + sweepAngle.half)
            val textY = drawOval.centerY() + radius.half.translateYByAngle(startAngle + sweepAngle.half)

            val textBounds = textComponent.getTextBounds(context, label)

            textBounds.offset(
                textX - textBounds.width().toInt().half,
                textY - textBounds.height().toInt().half,
            )

            layoutHelper.adjustTextBounds(textBounds, slicePath)

            textComponent.drawText(
                context = context,
                text = label,
                textX = textBounds.centerX(),
                textY = textY,
                maxTextWidth = textBounds.width().toInt(),
            )
        }

        protected fun MeasureContext.applyOffset(rectF: RectF, angle: Float) {
            rectF.offset(
                offsetFromCenterDp.pixels.translateXByAngle(angle),
                offsetFromCenterDp.pixels.translateYByAngle(angle),
            )
        }
    }

    override val bounds: RectF = RectF()

    protected val spacingPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    protected val oval: RectF = RectF()

    protected val spacingPathBuilder: Path = Path()

    protected val spacingPath: Path = Path()

    protected val spacingMatrix: Matrix = Matrix()

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

        val restoreCount = if (spacingDp > 0f) saveLayer() else -1

        spacingPath.rewind()

        model.entries.foldIndexed(startAngle) { index, startAngle, entry ->

            val slice = slices.getRepeating(index)

            val sweepAngle = entry.value / model.maxValue * FULL_DEGREES

            slice.draw(
                context = context,
                oval = oval,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                label = entry.label,
            )

            if (spacingDp > 0f) {
                addSpacingSegment(spacingPathBuilder, startAngle)
            }

            startAngle + sweepAngle
        }

        canvas.drawPath(spacingPath, spacingPaint)
        if (restoreCount >= 0) restoreCanvasToCount(restoreCount)
    }

    protected open fun DrawContext.addSpacingSegment(
        pathBuilder: Path,
        angle: Float,
    ) {
        val spacing = spacingDp.pixels
        with(spacingPathBuilder) {
            spacingMatrix.postRotate(angle, oval.centerX(), oval.centerY())
            rewind()
            moveTo(oval.centerX(), oval.centerY() + spacing.half)
            lineTo(oval.right + spacing, oval.centerY() + spacing.half)
            lineTo(oval.right + spacing, oval.centerY() - spacing.half)
            lineTo(oval.centerX(), oval.centerY() - spacing.half)
            close()
            transform(spacingMatrix)
            spacingPath.addPath(spacingPathBuilder)
            spacingMatrix.reset()
        }
    }
}
