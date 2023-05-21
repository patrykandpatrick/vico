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

package com.patrykandpatrick.vico.core.chart.pie.label

import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.component.shape.PathComponent
import com.patrykandpatrick.vico.core.component.text.HorizontalPosition
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.constants.FULL_DEGREES
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.extension.ceil
import com.patrykandpatrick.vico.core.extension.centerPoint
import com.patrykandpatrick.vico.core.extension.component1
import com.patrykandpatrick.vico.core.extension.component2
import com.patrykandpatrick.vico.core.extension.component3
import com.patrykandpatrick.vico.core.extension.component4
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.radius
import com.patrykandpatrick.vico.core.math.radians
import com.patrykandpatrick.vico.core.math.translatePointByAngle
import com.patrykandpatrick.vico.core.model.Point
import kotlin.math.abs
import kotlin.math.sin

/**
 * TODO
 *
 * @param textComponent TODO
 * @param maxWidthToBoundsRatio TODO
 * @param lineColor the color of the line.
 * @param lineWidthDp the thickness of the line.
 * @param angledSegmentLengthDp the thickness of the angled line segment of the label.
 * @param horizontalSegmentWidthDp the thickness of the horizontal line segment of the label.
 */
public open class OutsideSliceLabel(
    override var textComponent: TextComponent = textComponent(),
    lineColor: Int = Color.BLACK,
    lineWidthDp: Float = 1f,
    public var angledSegmentLengthDp: Float = DefaultDimens.SLICE_ANGLED_SEGMENT_WIDTH,
    public var horizontalSegmentWidthDp: Float = DefaultDimens.SLICE_HORIZONTAL_SEGMENT_WIDTH,
    public var maxWidthToBoundsRatio: Float = DefaultDimens.SLICE_OUTSIDE_LABEL_MAX_WIDTH_TO_BOUNDS_RATIO,
) : SliceLabel() {

    protected var measuredTextWidth: Int = 0

    protected var line: PathComponent = PathComponent(
        color = Color.TRANSPARENT,
        strokeWidthDp = lineWidthDp,
        strokeColor = lineColor,
    )

    /**
     * The color of the line.
     */
    public var lineColor: Int by line::strokeColor

    /**
     * The width of the line.
     */
    public var lineWidthDp: Float by line::strokeWidthDp

    init {
        require(maxWidthToBoundsRatio < 1f) { "The `maxWidthToBoundsRatio` cannot be higher than 1." }
    }

    protected fun getTextMaxWidth(contentBounds: RectF): Int =
        (contentBounds.width() * maxWidthToBoundsRatio).ceil.toInt()

    override fun getInsets(
        context: DrawContext,
        contentBounds: RectF,
        oval: RectF,
        angle: Float,
        label: CharSequence,
        outInsets: Insets,
    ): Unit = with(context) {
        val finalLinePoint = getFinalLinePoint(oval, angle)

        val (leftBound, topBound, rightBound, bottomBound) = contentBounds

        val availableWidth = if (finalLinePoint.x < oval.centerX()) {
            finalLinePoint.x - leftBound
        } else {
            rightBound - finalLinePoint.x
        }.toInt()

        measuredTextWidth = getTextMaxWidth(contentBounds).coerceAtLeast(availableWidth)

        val textBounds = textComponent.getTextBounds(
            context = context,
            text = label,
            width = measuredTextWidth,
        )

        val alpha = angle.radians

        val left = if (finalLinePoint.x < oval.centerX()) {
            val leftInset = (leftBound + finalLinePoint.x - textBounds.width()) / sin(-Math.PI.half - alpha)
            if (leftInset < 0) abs(leftInset).toFloat() else 0f
        } else {
            0f
        }

        val top = if (finalLinePoint.y < oval.centerY()) {
            val topInset = (topBound + finalLinePoint.y - textBounds.height().half) / sin(-alpha)
            if (topInset < 0) abs(topInset) else 0f
        } else {
            0f
        }

        val right = if (finalLinePoint.x > oval.centerX()) {
            val rightInset = (finalLinePoint.x + textBounds.width() - rightBound) / sin(Math.PI.half - alpha)
            if (rightInset > 0) rightInset.toFloat() else 0f
        } else {
            0f
        }

        val bottom = if (finalLinePoint.y > oval.centerY()) {
            val bottomInset = (finalLinePoint.y + textBounds.height().half - bottomBound) / sin(alpha)
            if (bottomInset > 0) bottomInset else 0f
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
        offsetFromCenter: Float,
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
        val radiusWithTranslation = drawOval.radius + angledSegmentLengthDp.pixels

        val (baseX, y) = translatePointByAngle(
            center = drawOval.centerPoint,
            point = Point(
                x = drawOval.centerX() + radiusWithTranslation,
                y = drawOval.centerY(),
            ),
            angle = Math.toRadians(angle.toDouble()),
        )

        return Point(
            x = baseX + horizontalSegmentWidthDp.pixels * if (baseX < drawOval.centerX()) -1f else 1f,
            y = y,
        )
    }

    protected open fun DrawContext.drawLine(
        drawOval: RectF,
        angle: Float,
    ): Point {
        var linePoint = translatePointByAngle(
            center = drawOval.centerPoint,
            point = Point(
                x = drawOval.centerX() + drawOval.radius,
                y = drawOval.centerY(),
            ),
            angle = Math.toRadians(angle.toDouble()),
        )

        line.draw(this) {
            moveTo(linePoint)

            lineTo(
                point = translatePointByAngle(
                    center = drawOval.centerPoint,
                    point = Point(
                        x = drawOval.centerX() + drawOval.radius + angledSegmentLengthDp.pixels,
                        y = drawOval.centerY(),
                    ),
                    angle = Math.toRadians(angle.toDouble()),
                ),
            )

            rLineTo(
                x = horizontalSegmentWidthDp.pixels * if (lastX < drawOval.centerX()) -1f else 1f,
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
