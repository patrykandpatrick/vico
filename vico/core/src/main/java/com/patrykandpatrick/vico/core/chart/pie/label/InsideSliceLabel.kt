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

package com.patrykandpatrick.vico.core.chart.pie.label

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.constants.ELLIPSIS
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.extension.ceil
import com.patrykandpatrick.vico.core.extension.centerPoint
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.layout.PieLayoutHelper
import com.patrykandpatrick.vico.core.math.radiansDouble
import com.patrykandpatrick.vico.core.math.translatePointByAngle
import com.patrykandpatrick.vico.core.util.Point

/**
 * A label for a slice used in a pie chart. The label is drawn inside the slice.
 *
 * @param textComponent the [TextComponent] used to draw the label.
 */
public open class InsideSliceLabel(
    public var textComponent: TextComponent,
) : SliceLabel() {
    protected val layoutHelper: PieLayoutHelper = PieLayoutHelper()

    protected val sliceBounds: RectF = RectF()

    protected val transform: Matrix = Matrix()

    public override fun drawLabel(
        context: DrawContext,
        oval: RectF,
        holeRadius: Float,
        angle: Float,
        slicePath: Path,
        label: CharSequence,
        sliceOpacity: Float,
        labelOpacity: Float,
    ): Unit =
        with(context) {
            val radius = oval.width().half

            transform.setRotate(-angle, oval.centerX(), oval.centerY())
            slicePath.transform(transform)
            slicePath.computeBounds(sliceBounds, false)
            transform.setRotate(angle, oval.centerX(), oval.centerY())
            slicePath.transform(transform)

            val (textX, textY) =
                translatePointByAngle(
                    center = oval.centerPoint,
                    point =
                        Point(
                            x = oval.centerX() + holeRadius + (radius - holeRadius).half,
                            y = oval.centerY(),
                        ),
                    angle = angle.radiansDouble,
                )

            var minWidth = textComponent.getTextBounds(this, ELLIPSIS).width()
            val textBounds = textComponent.getTextBounds(this, label)
            minWidth = minWidth.coerceAtMost(textBounds.width())
            textBounds.offset(
                textX - textBounds.width().toInt().half,
                textY - textBounds.height().toInt().half,
            )

            layoutHelper.adjustTextBounds(textBounds, slicePath)

            val maxTextWidth = textBounds.width().ceil.toInt()

            if (maxTextWidth > minWidth) {
                textComponent.drawText(
                    context = this,
                    text = label,
                    textX = textBounds.centerX(),
                    textY = textY,
                    maxTextWidth = textBounds.width().ceil.toInt(),
                    opacity = labelOpacity,
                )
            }
        }
}
