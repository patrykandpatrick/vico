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

package pl.patrykgoworowski.vico.view.theme

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import pl.patrykgoworowski.vico.core.DefaultDimens
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.OverlayingComponent
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.view.R
import pl.patrykgoworowski.vico.view.extension.defaultColors

internal fun TypedArray.getLineComponent(
    context: Context,
    defaultColor: Int = context.defaultColors.axisLineColor.toInt(),
    defaultThickness: Float = DefaultDimens.AXIS_LINE_WIDTH,
    defaultShape: Shape = Shapes.rectShape,
): LineComponent = use { array ->
    LineComponent(
        color = array.getColor(
            index = R.styleable.LineComponentStyle_color,
            defaultColor = defaultColor,
        ),
        thicknessDp = array.getRawDimension(
            context = context,
            index = R.styleable.LineComponentStyle_thickness,
            defaultValue = defaultThickness,
        ),
        shape = if (hasValue(R.styleable.LineComponentStyle_shapeStyle)) {
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.LineComponentStyle_shapeStyle,
                styleableResourceId = R.styleable.Shape,
            ).getShape(
                context = context,
            )
        } else {
            defaultShape
        },
        strokeColor = array.getColor(
            index = R.styleable.LineComponentStyle_strokeColor,
            defaultColor = Color.TRANSPARENT,
        ),
        strokeWidthDp = array.getRawDimension(
            context = context,
            index = R.styleable.LineComponentStyle_strokeWidth,
            defaultValue = 0f,
        ),
    )
}

internal fun TypedArray.getComponent(
    context: Context,
): Component? = use { array ->

    if (!hasValue(R.styleable.ComponentStyle_color)) {
        return@use null
    }

    val overlayingComponent = if (hasValue(R.styleable.ComponentStyle_overlayingComponentStyle)) {
        getNestedTypedArray(
            context = context,
            resourceId = R.styleable.ComponentStyle_overlayingComponentStyle,
            styleableResourceId = R.styleable.ComponentStyle,
        ).getComponent(context)
    } else {
        null
    }

    val baseComponent = ShapeComponent(
        color = array.getColor(index = R.styleable.ComponentStyle_color),
        shape = getNestedTypedArray(
            context = context,
            resourceId = R.styleable.ComponentStyle_shapeStyle,
            styleableResourceId = R.styleable.Shape,
        ).getShape(context),
        strokeColor = array.getColor(
            index = R.styleable.ComponentStyle_strokeColor,
            defaultColor = Color.TRANSPARENT,
        ),
        strokeWidthDp = array.getRawDimension(
            context = context,
            index = R.styleable.ComponentStyle_strokeWidth,
            defaultValue = 0f,
        ),
    )

    if (overlayingComponent != null) {
        OverlayingComponent(
            outer = baseComponent,
            inner = overlayingComponent,
            innerPaddingAllDp = getRawDimension(
                context = context,
                index = R.styleable.ComponentStyle_overlayingComponentPadding,
                defaultValue = 0f,
            )
        )
    } else {
        baseComponent
    }
}
