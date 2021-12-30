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

package pl.patrykgoworowski.vico.core.axis

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.chart.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.dimensions.BoundsAware
import pl.patrykgoworowski.vico.core.chart.insets.ChartInsetter
import pl.patrykgoworowski.vico.core.context.MeasureContext

public interface AxisRenderer<Position : AxisPosition> : BoundsAware, ChartInsetter {

    public val position: Position
    public val chartBounds: RectF
    public val MeasureContext.axisThickness: Float
    public val MeasureContext.tickThickness: Float
    public val MeasureContext.guidelineThickness: Float
    public val tickLengthDp: Float
    public val MeasureContext.tickLength: Float
    public val restrictedBounds: List<RectF>

    public val MeasureContext.maxAnyAxisLineThickness: Float
        get() = maxOf(axisThickness, tickThickness, guidelineThickness)

    public val labelLineHeight: Int
        get() = label?.lineHeight ?: 0

    public val labelAllLinesHeight: Int
        get() = label?.allLinesHeight ?: 0

    public var label: TextComponent?
    public var axis: LineComponent?
    public var tick: LineComponent?
    public var guideline: LineComponent?
    public var isLtr: Boolean
    public var valueFormatter: AxisValueFormatter

    public fun drawBehindChart(
        context: ChartDrawContext,
    )

    public fun drawAboveChart(
        context: ChartDrawContext,
    )

    public fun setChartBounds(
        left: Number,
        top: Number,
        right: Number,
        bottom: Number
    )

    public fun setChartBounds(bounds: RectF) {
        setChartBounds(
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.bottom
        )
    }

    public fun getDesiredWidth(
        context: MeasureContext,
        labels: List<String>,
    ): Float

    public fun setRestrictedBounds(vararg bounds: RectF?)

    public fun getDesiredHeight(context: MeasureContext): Int
}
