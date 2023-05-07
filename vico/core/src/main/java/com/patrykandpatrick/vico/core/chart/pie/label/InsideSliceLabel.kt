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

import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.extension.ceil
import com.patrykandpatrick.vico.core.extension.centerPoint
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.math.translatePointByAngle
import com.patrykandpatrick.vico.core.model.Point

/**
 * TODO
 */
public open class InsideSliceLabel(
    override var textComponent: TextComponent = textComponent(),
) : SliceLabel() {

    public override fun drawLabel(
        context: DrawContext,
        contentBounds: RectF,
        oval: RectF,
        angle: Float,
        offsetFromCenter: Float,
        slicePath: Path,
        label: CharSequence,
    ): Unit = with(context) {

        val radius = oval.width().half

        val (textX, textY) = translatePointByAngle(
            center = oval.centerPoint,
            point = Point(
                x = oval.centerX() + (radius + offsetFromCenter).half,
                y = oval.centerY(),
            ),
            angle = Math.toRadians(angle.toDouble()),
        )

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
            maxTextWidth = textBounds.width().ceil.toInt(),
        )
    }
}
