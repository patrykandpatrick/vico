/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import android.util.AttributeSet
import androidx.annotation.StyleableRes
import pl.patrykgoworowski.vico.core.axis.Axis
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.axisBuilder
import pl.patrykgoworowski.vico.core.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.view.R

internal class ThemeHandler(
    val context: Context,
    attrs: AttributeSet?,
) {

    public var startAxis: VerticalAxis<AxisPosition.Vertical.Start>? = null
        private set

    public var topAxis: HorizontalAxis<AxisPosition.Horizontal.Top>? = null
        private set

    public var endAxis: VerticalAxis<AxisPosition.Vertical.End>? = null
        private set

    public var bottomAxis: HorizontalAxis<AxisPosition.Horizontal.Bottom>? = null
        private set

    init {
        context.obtainStyledAttributes(attrs, R.styleable.DataSetView).use { typedArray ->
            if (typedArray.getBoolean(R.styleable.DataSetView_showStartAxis, false)) {
                startAxis = VerticalAxis.Builder(typedArray.getAxis()).build()
            }
            if (typedArray.getBoolean(R.styleable.DataSetView_showTopAxis, false)) {
                topAxis = HorizontalAxis.Builder(typedArray.getAxis()).build()
            }
            if (typedArray.getBoolean(R.styleable.DataSetView_showEndAxis, false)) {
                endAxis = VerticalAxis.Builder(typedArray.getAxis()).build()
            }
            if (typedArray.getBoolean(R.styleable.DataSetView_showBottomAxis, false)) {
                bottomAxis = HorizontalAxis.Builder(typedArray.getAxis()).build()
            }
        }
    }

    private fun TypedArray.getNestedTypedArray(
        @StyleableRes resourceId: Int,
        @StyleableRes styleableResourceId: IntArray,
    ): TypedArray =
        getResourceId(resourceId, 0)
            .let { resId -> context.obtainStyledAttributes(resId, styleableResourceId) }

    private fun TypedArray.getAxis(): Axis.Builder {

        fun TypedArray.getLineComponent(
            @StyleableRes resourceId: Int,
            @StyleableRes styleableResourceId: IntArray,
        ): LineComponent =
            getNestedTypedArray(
                resourceId = resourceId,
                styleableResourceId = styleableResourceId,
            ).getLineComponent(
                context = context,
                colorStyleableRes = R.styleable.AxisLine_color,
                thicknessStyleableRes = R.styleable.AxisLine_thickness,
            )

        return getNestedTypedArray(R.styleable.DataSetView_axisStyle, R.styleable.Axis)
            .use { axisStyle ->
                axisBuilder {
                    axis = axisStyle.getLineComponent(
                        resourceId = R.styleable.Axis_axisLineStyle,
                        styleableResourceId = R.styleable.AxisLine
                    )
                    tick = axisStyle.getLineComponent(
                        resourceId = R.styleable.Axis_axisGuidelineStyle,
                        styleableResourceId = R.styleable.AxisLine
                    )
                    guideline = axisStyle.getLineComponent(
                        resourceId = R.styleable.Axis_axisTickStyle,
                        styleableResourceId = R.styleable.AxisLine
                    )
                }
            }
    }
}
