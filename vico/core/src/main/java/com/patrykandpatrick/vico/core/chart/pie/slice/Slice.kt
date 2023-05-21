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

package com.patrykandpatrick.vico.core.chart.pie.slice

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatrick.vico.core.chart.pie.label.SliceLabel
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.shape.shadow.PaintComponent
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.ifNotNull
import com.patrykandpatrick.vico.core.extension.isNotTransparent
import com.patrykandpatrick.vico.core.extension.isTransparent
import com.patrykandpatrick.vico.core.extension.round
import com.patrykandpatrick.vico.core.extension.updateBy
import com.patrykandpatrick.vico.core.layout.PieLayoutHelper
import com.patrykandpatrick.vico.core.math.translatePointByAngle
import com.patrykandpatrick.vico.core.model.Point

/**
 * TODO
 *
 * @param color the color of the slice.
 * @param dynamicShader TODO
 * @param strokeWidthDp TODO
 * @param strokeColor TODO
 * @param offsetFromCenterDp TODO
 * @param label TODO
 */
public open class Slice(
    public var color: Int = Color.LTGRAY,
    public var dynamicShader: DynamicShader? = null,
    public var strokeWidthDp: Float = 0f,
    public var strokeColor: Int = Color.TRANSPARENT,
    public var offsetFromCenterDp: Float = 0f,
    public var label: SliceLabel? = null,
) : PaintComponent<Slice>() {

    protected val layoutHelper: PieLayoutHelper = PieLayoutHelper()

    protected val fillPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
    }

    protected val strokePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = strokeColor
    }

    protected val drawOval: RectF = RectF()

    protected val slicePath: Path = Path()

    protected val sliceBounds: RectF = RectF()

    /**
     * TODO
     */
    @LongParameterListDrawFunction
    public open fun draw(
        context: DrawContext,
        contentBounds: RectF,
        oval: RectF,
        startAngle: Float,
        sweepAngle: Float,
        holeRadius: Float,
        label: CharSequence?,
        spacingPath: Path,
    ): Unit = with(context) {
        drawOval.set(oval)
        applyOffset(drawOval, startAngle + sweepAngle.half)

        if (color.isNotTransparent) {
            maybeUpdateShadowLayer(context, fillPaint, fillPaint.color)
            drawFilledSlice(context, startAngle, sweepAngle, spacingPath)
        }

        if (strokeColor.isNotTransparent && strokeWidthDp > 0f) {
            if (color.isTransparent) {
                maybeUpdateShadowLayer(context, strokePaint, strokePaint.color)
            }

            drawStrokedSlice(context, startAngle, sweepAngle, spacingPath)
        }

        ifNotNull(this@Slice.label, label) { labelComponent, label ->
            labelComponent.drawLabel(
                context = context,
                contentBounds = contentBounds,
                oval = drawOval,
                angle = startAngle + sweepAngle.half,
                offsetFromCenter = holeRadius - offsetFromCenterDp.pixels,
                slicePath = slicePath,
                label = label,
            )
        }
    }

    protected open fun drawFilledSlice(
        context: DrawContext,
        startAngle: Float,
        sweepAngle: Float,
        spacingPath: Path,
    ): Unit = with(context) {
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = color

        slicePath.rewind()

        slicePath.addArc(drawOval, startAngle, sweepAngle)

        slicePath.lineTo(drawOval.centerX(), drawOval.centerY())

        slicePath.close()

        if (dynamicShader != null) {
            slicePath.computeBounds(sliceBounds, false)
            fillPaint.shader = dynamicShader?.provideShader(context, sliceBounds)
        }

        if (spacingPath.isEmpty.not()) {
            slicePath.op(spacingPath, Path.Op.DIFFERENCE)
        }

        canvas.drawPath(slicePath, fillPaint)
    }

    protected open fun drawStrokedSlice(
        context: DrawContext,
        startAngle: Float,
        sweepAngle: Float,
        spacingPath: Path,
    ): Unit = with(context) {
        val strokeWidth = strokeWidthDp.pixels

        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = strokeColor
        strokePaint.strokeWidth = strokeWidth

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

        if (spacingPath.isEmpty.not()) {
            slicePath.op(spacingPath, Path.Op.DIFFERENCE)
        }

        canvas.drawPath(slicePath, strokePaint)
    }

    protected fun MeasureContext.applyOffset(rectF: RectF, angle: Float) {
        val (dx, dy) = translatePointByAngle(
            center = Point(0, 0),
            point = Point(
                x = offsetFromCenterDp.pixels,
                y = 0f,
            ),
            angle = Math.toRadians(angle.toDouble()),
        )

        rectF.offset(dx.round, dy.round)
    }
}
