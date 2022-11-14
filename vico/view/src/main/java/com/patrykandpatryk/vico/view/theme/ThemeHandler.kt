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

package com.patrykandpatryk.vico.view.theme

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.StyleableRes
import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.axis.Axis
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatryk.vico.core.chart.Chart
import com.patrykandpatryk.vico.core.chart.column.ColumnChart
import com.patrykandpatryk.vico.core.chart.column.ColumnChart.MergeMode
import com.patrykandpatryk.vico.core.chart.composed.ComposedChart
import com.patrykandpatryk.vico.core.chart.composed.ComposedChartEntryModel
import com.patrykandpatryk.vico.core.component.shape.DashedShape
import com.patrykandpatryk.vico.core.component.shape.LineComponent
import com.patrykandpatryk.vico.core.component.shape.Shape
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.extension.hasAnyFlagOf
import com.patrykandpatryk.vico.core.extension.hasFlag
import com.patrykandpatryk.vico.view.R

internal class ThemeHandler(
    private val context: Context,
    attrs: AttributeSet?,
    chartType: ChartType,
) {

    public var startAxis: Axis<AxisPosition.Vertical.Start>? = null
        private set

    public var topAxis: Axis<AxisPosition.Horizontal.Top>? = null
        private set

    public var endAxis: Axis<AxisPosition.Vertical.End>? = null
        private set

    public var bottomAxis: Axis<AxisPosition.Horizontal.Bottom>? = null
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
                startAxis = typedArray.getAxisBuilder(
                    R.styleable.BaseChartView_startAxisStyle,
                    VerticalAxis.Builder(),
                ).build()
            }
            if (typedArray.getBoolean(R.styleable.BaseChartView_showTopAxis, false)) {
                topAxis = typedArray.getAxisBuilder(
                    R.styleable.BaseChartView_topAxisStyle,
                    HorizontalAxis.Builder(),
                ).build()
            }
            if (typedArray.getBoolean(R.styleable.BaseChartView_showEndAxis, false)) {
                endAxis = typedArray.getAxisBuilder(
                    R.styleable.BaseChartView_endAxisStyle,
                    VerticalAxis.Builder(),
                ).build()
            }
            if (typedArray.getBoolean(R.styleable.BaseChartView_showBottomAxis, false)) {
                bottomAxis = typedArray.getAxisBuilder(
                    R.styleable.BaseChartView_bottomAxisStyle,
                    HorizontalAxis.Builder(),
                ).build()
            }
            isHorizontalScrollEnabled = typedArray
                .getBoolean(R.styleable.BaseChartView_chartHorizontalScrollingEnabled, true)
            isChartZoomEnabled = typedArray
                .getBoolean(R.styleable.BaseChartView_chartZoomEnabled, true)
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

    private fun <Position : AxisPosition, Builder : Axis.Builder<Position>> TypedArray.getAxisBuilder(
        styleAttrId: Int,
        builder: Builder,
    ): Builder {

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

        val axisStyle = getNestedTypedArray(
            context = context,
            resourceId = if (hasValue(styleAttrId)) styleAttrId else R.styleable.BaseChartView_axisStyle,
            styleableResourceId = R.styleable.Axis,
        )

        return builder.apply {
            axis = axisStyle.getLineComponent(
                resourceId = R.styleable.Axis_axisLineStyle,
                styleableResourceId = R.styleable.LineComponent,
            )
            tick = axisStyle.getLineComponent(
                resourceId = R.styleable.Axis_axisTickStyle,
                styleableResourceId = R.styleable.LineComponent,
            )
            tickLengthDp = axisStyle.getRawDimension(
                context = context,
                R.styleable.Axis_axisTickLength,
                defaultValue = DefaultDimens.AXIS_TICK_LENGTH,
            )
            guideline = axisStyle.getLineComponent(
                resourceId = R.styleable.Axis_axisGuidelineStyle,
                styleableResourceId = R.styleable.LineComponent,
                defaultShape = DashedShape(),
            )
            labelRotationDegrees = axisStyle.getFloat(
                R.styleable.Axis_labelRotationDegrees,
                0f,
            )
            label = axisStyle.getNestedTypedArray(
                context = context,
                resourceId = R.styleable.Axis_axisLabelStyle,
                styleableResourceId = R.styleable.TextComponentStyle,
            ).getTextComponent(context = context)
            titleComponent = if (axisStyle.getBoolean(R.styleable.Axis_showTitle, false)) {
                axisStyle.getNestedTypedArray(
                    context = context,
                    resourceId = R.styleable.Axis_titleStyle,
                    styleableResourceId = R.styleable.TextComponentStyle,
                ).getTextComponent(context = context)
            } else null
            title = axisStyle.getString(R.styleable.Axis_title)

            when (this) {
                is VerticalAxis.Builder<*> -> {
                    horizontalLabelPosition = axisStyle
                        .getInteger(R.styleable.Axis_verticalAxisHorizontalLabelPosition, 0)
                        .let { value ->
                            val values = VerticalAxis.HorizontalLabelPosition.values()
                            values[value % values.size]
                        }

                    verticalLabelPosition = axisStyle
                        .getInteger(R.styleable.Axis_verticalAxisVerticalLabelPosition, 0)
                        .let { value ->
                            val values = VerticalAxis.VerticalLabelPosition.values()
                            values[value % values.size]
                        }
                }
                is HorizontalAxis.Builder<*> -> {
                    tickPosition = when (axisStyle.getInteger(R.styleable.Axis_horizontalAxisTickPosition, 0)) {
                        0 -> HorizontalAxis.TickPosition.Edge
                        else -> HorizontalAxis.TickPosition.Center(
                            offset = axisStyle.getInteger(R.styleable.Axis_horizontalAxisTickOffset, 0),
                            spacing = axisStyle.getInteger(R.styleable.Axis_horizontalAxisTickSpacing, 1),
                        )
                    }
                }
            }
        }.also { axisStyle.recycle() }
    }

    private fun TypedArray.getChart(): Chart<ChartEntryModel>? =
        when (getInt(R.styleable.ChartView_chart, 0)) {
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
                mergeMode = if (chartFlags.hasFlag(STACKED_COLUMN_CHART)) MergeMode.Stack else MergeMode.Grouped,
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
    }

    private companion object {
        const val COLUMN_CHART = 1
        const val STACKED_COLUMN_CHART = 2
        const val LINE_CHART = 4
    }
}
