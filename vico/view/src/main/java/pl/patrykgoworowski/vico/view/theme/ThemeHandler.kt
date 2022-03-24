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
import android.util.AttributeSet
import androidx.annotation.StyleableRes
import pl.patrykgoworowski.vico.core.DefaultDimens
import pl.patrykgoworowski.vico.core.axis.Axis
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.axisBuilder
import pl.patrykgoworowski.vico.core.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis
import pl.patrykgoworowski.vico.core.chart.Chart
import pl.patrykgoworowski.vico.core.chart.column.ColumnChart
import pl.patrykgoworowski.vico.core.chart.column.ColumnChart.MergeMode
import pl.patrykgoworowski.vico.core.chart.composed.ComposedChart
import pl.patrykgoworowski.vico.core.chart.composed.ComposedChartEntryModel
import pl.patrykgoworowski.vico.core.component.shape.DashedShape
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.extension.hasAnyFlagOf
import pl.patrykgoworowski.vico.core.extension.hasFlag
import pl.patrykgoworowski.vico.view.R

internal class ThemeHandler(
    private val context: Context,
    attrs: AttributeSet?,
    chartType: ChartType,
) {

    public var startAxis: VerticalAxis<AxisPosition.Vertical.Start>? = null
        private set

    public var topAxis: HorizontalAxis<AxisPosition.Horizontal.Top>? = null
        private set

    public var endAxis: VerticalAxis<AxisPosition.Vertical.End>? = null
        private set

    public var bottomAxis: HorizontalAxis<AxisPosition.Horizontal.Bottom>? = null
        private set

    public var isHorizontalScrollEnabled: Boolean = false
        private set

    public var isChartZoomEnabled: Boolean = false
        private set

    public var chart: Chart<ChartEntryModel>? = null
        private set

    public var composedChart: Chart<ComposedChartEntryModel<ChartEntryModel>>? = null
        private set

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BaseChartView).use { typedArray ->
            if (typedArray.getBoolean(R.styleable.BaseChartView_showStartAxis, false)) {
                startAxis = VerticalAxis.Builder<AxisPosition.Vertical.Start>(typedArray.getAxis()).build()
            }
            if (typedArray.getBoolean(R.styleable.BaseChartView_showTopAxis, false)) {
                topAxis = HorizontalAxis.Builder<AxisPosition.Horizontal.Top>(typedArray.getAxis()).build()
            }
            if (typedArray.getBoolean(R.styleable.BaseChartView_showEndAxis, false)) {
                endAxis = VerticalAxis.Builder<AxisPosition.Vertical.End>(typedArray.getAxis()).build()
            }
            if (typedArray.getBoolean(R.styleable.BaseChartView_showBottomAxis, false)) {
                bottomAxis = HorizontalAxis.Builder<AxisPosition.Horizontal.Bottom>(typedArray.getAxis()).build()
            }
            isHorizontalScrollEnabled = typedArray
                .getBoolean(R.styleable.BaseChartView_chartHorizontalScrollingEnabled, false)
            isChartZoomEnabled = typedArray
                .getBoolean(R.styleable.BaseChartView_chartZoomEnabled, false)
        }
        when (chartType) {
            ChartType.Single ->
                context.obtainStyledAttributes(attrs, R.styleable.ChartView).use { typedArray ->
                    chart = typedArray.getChart()
                }
            ChartType.Composed ->
                context.obtainStyledAttributes(attrs, R.styleable.ComposedChartView).use { typedArray ->
                    composedChart = typedArray.getComposedChart()
                }
        }
    }

    private fun <Position : AxisPosition> TypedArray.getAxis(): Axis.Builder<Position> {

        fun TypedArray.getLineComponent(
            @StyleableRes resourceId: Int,
            @StyleableRes styleableResourceId: IntArray,
            defaultShape: Shape = Shapes.rectShape,
        ): LineComponent =
            getNestedTypedArray(
                context = context,
                resourceId = resourceId,
                styleableResourceId = styleableResourceId,
            ).getLineComponent(context = context, defaultShape = defaultShape)

        return getNestedTypedArray(
            context = context,
            resourceId = R.styleable.BaseChartView_axisStyle,
            styleableResourceId = R.styleable.Axis,
        ).use { axisStyle ->
            axisBuilder {
                axis = axisStyle.getLineComponent(
                    resourceId = R.styleable.Axis_axisLineStyle,
                    styleableResourceId = R.styleable.LineComponentStyle
                )
                tick = axisStyle.getLineComponent(
                    resourceId = R.styleable.Axis_axisTickStyle,
                    styleableResourceId = R.styleable.LineComponentStyle
                )
                tickLengthDp = axisStyle.getRawDimension(
                    context = context,
                    R.styleable.Axis_axisTickLength,
                    defaultValue = DefaultDimens.AXIS_TICK_LENGTH,
                )
                guideline = axisStyle.getLineComponent(
                    resourceId = R.styleable.Axis_axisGuidelineStyle,
                    styleableResourceId = R.styleable.LineComponentStyle,
                    defaultShape = DashedShape(),
                )
                label = axisStyle.getNestedTypedArray(
                    context = context,
                    resourceId = R.styleable.Axis_axisLabelStyle,
                    styleableResourceId = R.styleable.LabelStyle
                ).getTextComponent(context = context)
            }
        }
    }

    private fun TypedArray.getChart(): Chart<ChartEntryModel>? =
        when (getInt(R.styleable.ChartView_chartType, 0)) {
            COLUMN_CHART -> getColumnChart(context, mergeMode = MergeMode.Grouped)
            STACKED_COLUMN_CHART -> getColumnChart(context, mergeMode = MergeMode.Stack)
            LINE_CHART -> getLineChart(context)
            else -> null
        }

    private fun TypedArray.getComposedChart(): Chart<ComposedChartEntryModel<ChartEntryModel>>? {
        val chartFlags = getInt(R.styleable.ComposedChartView_charts, 0)

        val columnChart: ColumnChart? = if (chartFlags.hasAnyFlagOf(COLUMN_CHART, STACKED_COLUMN_CHART)) {
            getColumnChart(
                context,
                mergeMode = if (chartFlags.hasFlag(STACKED_COLUMN_CHART)) MergeMode.Stack else MergeMode.Grouped
            )
        } else {
            null
        }
        val lineChart = if (chartFlags.hasFlag(LINE_CHART)) {
            getLineChart(context)
        } else {
            null
        }

        return when {
            columnChart != null && lineChart != null -> ComposedChart(columnChart, lineChart)
            columnChart != null -> columnChart
            lineChart != null -> lineChart
            else -> null
        }
    }

    internal enum class ChartType {
        Single,
        Composed,
        Unknown,
    }

    private companion object {
        const val COLUMN_CHART = 1
        const val STACKED_COLUMN_CHART = 2
        const val LINE_CHART = 4
    }
}
