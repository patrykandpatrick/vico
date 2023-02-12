/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.views.theme

import android.animation.TimeInterpolator
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.animation.AccelerateInterpolator
import androidx.annotation.StyleableRes
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.FADING_EDGE_VISIBILITY_THRESHOLD_DP
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.candlestickentry.CandlestickEntryModel
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.column.ColumnChart.MergeMode
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.composed.ComposedChartEntryModel
import com.patrykandpatrick.vico.core.chart.edges.FadingEdges
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shape
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.hasAnyFlagOf
import com.patrykandpatrick.vico.core.extension.hasFlag
import com.patrykandpatrick.vico.views.R
import java.lang.Exception

public class ThemeHandler(
    private val context: Context,
    attrs: AttributeSet?,
    chartType: ChartType?,
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

    public var candlestickChart: Chart<CandlestickEntryModel>? = null
        private set

    public var fadingEdges: FadingEdges? = null
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
            fadingEdges = typedArray.getFadingEdges()
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

            ChartType.Candlestick -> Unit // TODO
            null -> Unit
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
            axis = axisStyle
                .takeIf { it.getBoolean(R.styleable.Axis_showAxisLine, true) }
                ?.getLineComponent(
                    resourceId = R.styleable.Axis_axisLineStyle,
                    styleableResourceId = R.styleable.LineComponent,
                )
            tick = axisStyle
                .takeIf { it.getBoolean(R.styleable.Axis_showTick, true) }
                ?.getLineComponent(
                    resourceId = R.styleable.Axis_axisTickStyle,
                    styleableResourceId = R.styleable.LineComponent,
                )
            tickLengthDp = axisStyle.getRawDimension(
                context = context,
                R.styleable.Axis_axisTickLength,
                defaultValue = DefaultDimens.AXIS_TICK_LENGTH,
            )
            guideline = axisStyle
                .takeIf { it.getBoolean(R.styleable.Axis_showGuideline, true) }
                ?.getLineComponent(
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

    @Suppress("TooGenericExceptionCaught")
    private fun TypedArray.getFadingEdges(): FadingEdges? {
        val edgesLength = getRawDimension(context, R.styleable.BaseChartView_fadingEdgeWidth, 0f)
        val startLength = getRawDimension(context, R.styleable.BaseChartView_startFadingEdgeWidth, edgesLength)
        val endLength = getRawDimension(context, R.styleable.BaseChartView_endFadingEdgeWidth, edgesLength)
        val threshold = getRawDimension(
            context,
            R.styleable.BaseChartView_fadingEdgeVisibilityThreshold,
            FADING_EDGE_VISIBILITY_THRESHOLD_DP,
        )

        return if (startLength > 0f || endLength > 0f) {

            val interpolatorClassName = getString(R.styleable.BaseChartView_fadingEdgeVisibilityInterpolator)

            val interpolator = if (interpolatorClassName != null) {
                try {
                    context.classLoader.loadClass(interpolatorClassName).newInstance() as? TimeInterpolator
                } catch (e: Exception) {
                    Log.e(
                        "ChartView",
                        "Caught exception when trying to instantiate $interpolatorClassName " +
                            "as fade interpolator.",
                    )
                    null
                }
            } else null

            FadingEdges(
                startEdgeWidthDp = startLength,
                endEdgeWidthDp = endLength,
                visibilityThresholdDp = threshold,
                visibilityInterpolator = interpolator ?: AccelerateInterpolator(),
            )
        } else null
    }

    public enum class ChartType {
        Single,
        Composed,
        Candlestick,
    }

    private companion object {
        const val COLUMN_CHART = 1
        const val STACKED_COLUMN_CHART = 2
        const val LINE_CHART = 4
    }
}
