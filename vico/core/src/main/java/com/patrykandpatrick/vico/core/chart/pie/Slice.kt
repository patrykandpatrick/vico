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

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.DefaultDimens.SLICE_ANGLED_SEGMENT_THICKNESS
import com.patrykandpatrick.vico.core.DefaultDimens.SLICE_HORIZONTAL_SEGMENT_THICKNESS
import com.patrykandpatrick.vico.core.DefaultDimens.SLICE_OUTSIDE_LABEL_MAX_WIDTH_TO_BOUNDS_RATIO
import com.patrykandpatrick.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.component.shape.PathComponent
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.text.HorizontalPosition
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.constants.FULL_DEGREES
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.ifNotNull
import com.patrykandpatrick.vico.core.extension.isNotTransparent
import com.patrykandpatrick.vico.core.extension.radius
import com.patrykandpatrick.vico.core.extension.round
import com.patrykandpatrick.vico.core.extension.updateBy
import com.patrykandpatrick.vico.core.layout.PieLayoutHelper
import com.patrykandpatrick.vico.core.math.translateXByAngle
import com.patrykandpatrick.vico.core.math.translateYByAngle
import com.patrykandpatrick.vico.core.model.Point
import kotlin.math.abs

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
    public var label: Label? = Label.Inside(),
) {

    /**
     * TODO
     */
    public sealed class Label {

        protected val layoutHelper: PieLayoutHelper = PieLayoutHelper()

        protected abstract var textComponent: TextComponent

        /**
         * TODO
         */
        @LongParameterListDrawFunction
        public abstract fun drawLabel(
            context: DrawContext,
            contentBounds: RectF,
            oval: RectF,
            angle: Float,
            slicePath: Path,
            label: CharSequence,
        )

        /**
         * TODO
         */
        @LongParameterListDrawFunction
        public open fun getInsets(
            context: DrawContext,
            contentBounds: RectF,
            oval: RectF,
            angle: Float,
            label: CharSequence,
            outInsets: Insets,
        ): Unit = Unit

        /**
         * TODO
         */
        public open class Inside(
            override var textComponent: TextComponent = textComponent(),
        ) : Label() {

            public override fun drawLabel(
                context: DrawContext,
                contentBounds: RectF,
                oval: RectF,
                angle: Float,
                slicePath: Path,
                label: CharSequence,
            ): Unit = with(context) {

                val radius = oval.width().half

                val textX = oval.centerX() + radius.half.translateXByAngle(angle)
                val textY = oval.centerY() + radius.half.translateYByAngle(angle)

                val textBounds = textComponent.getTextBounds(this, label)

                textBounds.offset(
                    textX - textBounds.width().toInt().half,
                    textY - textBounds.height().toInt().half,
                )

                layoutHelper.adjustTextBounds(textBounds, slicePath)

                textComponent.drawText(
                    context = this,
                    text = label,
                    textX = textBounds.centerX(),
                    textY = textY,
                    maxTextWidth = textBounds.width().toInt(),
                )
            }
        }

        /**
         * TODO
         *
         * @param textComponent TODO
         * @param maxWidthToBoundsRatio TODO
         * @param line TODO
         * @param angledSegmentThicknessDp the thickness of the angled line segment of the label.
         * @param horizontalSegmentThicknessDp the thickness of the horizontal line segment of the label.
         */
        public open class Outside(
            override var textComponent: TextComponent = textComponent(),
            public var line: PathComponent? = null,
            public var angledSegmentThicknessDp: Float = SLICE_ANGLED_SEGMENT_THICKNESS,
            public var horizontalSegmentThicknessDp: Float = SLICE_HORIZONTAL_SEGMENT_THICKNESS,
            public var maxWidthToBoundsRatio: Float = SLICE_OUTSIDE_LABEL_MAX_WIDTH_TO_BOUNDS_RATIO,
        ) : Label() {

            protected var measuredTextWidth: Int = 0

            init {
                require(maxWidthToBoundsRatio < 1f) { "The `maxWidthToBoundsRatio` cannot be higher than 1." }
            }

            protected fun getTextMaxWidth(contentBounds: RectF): Int =
                (contentBounds.width() * maxWidthToBoundsRatio).toInt()

            override fun getInsets(
                context: DrawContext,
                contentBounds: RectF,
                oval: RectF,
                angle: Float,
                label: CharSequence,
                outInsets: Insets,
            ): Unit = with(context) {

                val finalLinePoint = getFinalLinePoint(oval, angle)

                val availableWidth = if (finalLinePoint.x < oval.centerX()) {
                    finalLinePoint.x - contentBounds.left
                } else {
                    contentBounds.right - finalLinePoint.x
                }.toInt()

                measuredTextWidth = getTextMaxWidth(contentBounds).coerceAtLeast(availableWidth)

                val textBounds = textComponent.getTextBounds(
                    context,
                    text = label,
                    width = measuredTextWidth,
                )

                val radius = oval.width().half

                val arcEdgeX = oval.centerX() + radius.translateXByAngle(angle)
                val arcEdgeY = oval.centerY() + radius.translateYByAngle(angle)

                val labelWidth = abs(finalLinePoint.x - arcEdgeX) + textBounds.width()
                val labelHeight = textBounds.height()

                val radiusScale = (radius - labelWidth.coerceAtLeast(labelHeight)) / radius

                val left = if (finalLinePoint.x < oval.centerX()) {
                    labelWidth - (arcEdgeX - oval.left) * radiusScale
                } else {
                    0f
                }

                val top = if (finalLinePoint.y < oval.centerY()) {
                    abs(finalLinePoint.y - textBounds.height() - arcEdgeY)
                    labelHeight - (arcEdgeY - oval.top) * radiusScale
                } else {
                    0f
                }

                val right = if (finalLinePoint.x > oval.centerX()) {
                    labelWidth - (oval.right - arcEdgeX) * radiusScale
                } else {
                    0f
                }

                val bottom = if (finalLinePoint.y > oval.centerY()) {
                    finalLinePoint.y + textBounds.height() - arcEdgeY
                    labelHeight - (oval.bottom - arcEdgeY) * radiusScale
                } else {
                    0f
                }

                outInsets.setAllIfGreater(
                    start = if (isLtr) left else right,
                    top = top,
                    end = if (isLtr) right else left,
                    bottom = bottom,
                )
            }

            public override fun drawLabel(
                context: DrawContext,
                contentBounds: RectF,
                oval: RectF,
                angle: Float,
                slicePath: Path,
                label: CharSequence,
            ): Unit = with(context) {

                val (textX, textY) = drawLine(oval, angle)

                val textBounds = textComponent.getTextBounds(this, label, measuredTextWidth)

                textBounds.offset(
                    textX - textBounds.width().toInt().half,
                    textY - textBounds.height().toInt().half,
                )

                val horizontalPosition = when (angle % FULL_DEGREES) {
                    in horizontalPositionStartDegreesRange -> HorizontalPosition.Start
                    else -> HorizontalPosition.End
                }

                textComponent.drawText(
                    context = this,
                    text = label,
                    textX = textBounds.centerX(),
                    textY = textY,
                    horizontalPosition = horizontalPosition,
                    maxTextWidth = measuredTextWidth,
                )
            }

            protected open fun DrawContext.getFinalLinePoint(
                drawOval: RectF,
                angle: Float,
            ): Point {
                val radiusWithTranslation = drawOval.radius + angledSegmentThicknessDp.pixels

                val baseX = drawOval.centerX() + radiusWithTranslation.translateXByAngle(angle)

                return Point(
                    x = baseX + horizontalSegmentThicknessDp.pixels * if (baseX < drawOval.centerX()) -1f else 1f,
                    y = drawOval.centerY() + radiusWithTranslation.translateYByAngle(angle),
                )
            }

            protected open fun DrawContext.drawLine(
                drawOval: RectF,
                angle: Float,
            ): Point {

                var linePoint = Point(
                    drawOval.centerX() + drawOval.radius.translateXByAngle(angle),
                    drawOval.centerY() + drawOval.radius.translateYByAngle(angle),
                )

                line?.draw(this) {

                    moveTo(linePoint)

                    val translation = angledSegmentThicknessDp.pixels

                    lineTo(
                        x = drawOval.centerX() + (drawOval.radius + translation).translateXByAngle(angle),
                        y = drawOval.centerY() + (drawOval.radius + translation).translateYByAngle(angle),
                    )

                    rLineTo(
                        x = horizontalSegmentThicknessDp.pixels * if (lastX < drawOval.centerX()) -1f else 1f,
                        y = 0f,
                    )

                    linePoint = lastPoint
                }

                return linePoint
            }

            protected companion object {

                public val horizontalPositionStartDegreesRange: ClosedFloatingPointRange<Float> = 90f..270f
            }
        }
    }

    protected val layoutHelper: PieLayoutHelper = PieLayoutHelper()

    protected val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
    }

    protected val drawOval: RectF = RectF()

    protected val slicePath: Path = Path()

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

        ifNotNull(this@Slice.label, label) { labelComponent, label ->
            labelComponent.drawLabel(
                context = context,
                contentBounds = contentBounds,
                oval = drawOval,
                angle = startAngle + sweepAngle.half,
                slicePath = slicePath,
                label = label,
            )
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

    protected fun MeasureContext.applyOffset(rectF: RectF, angle: Float) {
        rectF.offset(
            offsetFromCenterDp.pixels.translateXByAngle(angle).round,
            offsetFromCenterDp.pixels.translateYByAngle(angle).round,
        )
    }
}
