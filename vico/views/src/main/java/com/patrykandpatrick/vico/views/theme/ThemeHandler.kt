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

package com.patrykandpatrick.vico.views.theme

import android.animation.TimeInterpolator
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.animation.AccelerateInterpolator
import androidx.annotation.StyleableRes
import com.patrykandpatrick.vico.core.DEF_LABEL_COUNT
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.FADING_EDGE_VISIBILITY_THRESHOLD_DP
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.CartesianChart
import com.patrykandpatrick.vico.core.chart.edges.FadingEdges
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shape
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.extension.hasFlag
import com.patrykandpatrick.vico.views.R

internal class ThemeHandler(
    private val context: Context,
    attrs: AttributeSet?,
) {
    var isHorizontalScrollEnabled: Boolean = false
        private set

    var isChartZoomEnabled: Boolean = false
        private set

    var chart: CartesianChart? = null
        private set

    lateinit var horizontalLayout: HorizontalLayout
        private set

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CartesianChartView).use { typedArray ->
            isHorizontalScrollEnabled =
                typedArray
                    .getBoolean(R.styleable.CartesianChartView_chartHorizontalScrollingEnabled, true)
            isChartZoomEnabled =
                typedArray
                    .getBoolean(R.styleable.CartesianChartView_chartZoomEnabled, true)
            horizontalLayout = typedArray.getHorizontalLayout()

            context
                .obtainStyledAttributes(attrs, R.styleable.CartesianChartView)
                .use { chart = getChart(it, typedArray) }
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

        val axisStyle =
            getNestedTypedArray(
                context = context,
                resourceId = if (hasValue(styleAttrId)) styleAttrId else R.styleable.CartesianChartView_axisStyle,
                styleableResourceId = R.styleable.Axis,
            )

        return builder.apply {
            axis =
                axisStyle
                    .takeIf { it.getBoolean(R.styleable.Axis_showAxisLine, true) }
                    ?.getLineComponent(
                        resourceId = R.styleable.Axis_axisLineStyle,
                        styleableResourceId = R.styleable.LineComponent,
                    )
            tick =
                axisStyle
                    .takeIf { it.getBoolean(R.styleable.Axis_showTick, true) }
                    ?.getLineComponent(
                        resourceId = R.styleable.Axis_axisTickStyle,
                        styleableResourceId = R.styleable.LineComponent,
                    )
            tickLengthDp =
                axisStyle.getRawDimension(
                    context = context,
                    R.styleable.Axis_axisTickLength,
                    defaultValue = DefaultDimens.AXIS_TICK_LENGTH,
                )
            guideline =
                axisStyle
                    .takeIf { it.getBoolean(R.styleable.Axis_showGuideline, true) }
                    ?.getLineComponent(
                        resourceId = R.styleable.Axis_axisGuidelineStyle,
                        styleableResourceId = R.styleable.LineComponent,
                        defaultShape = DashedShape(),
                    )
            labelRotationDegrees =
                axisStyle.getFloat(
                    R.styleable.Axis_labelRotationDegrees,
                    0f,
                )
            label =
                axisStyle.getNestedTypedArray(
                    context = context,
                    resourceId = R.styleable.Axis_axisLabelStyle,
                    styleableResourceId = R.styleable.TextComponentStyle,
                ).getTextComponent(context = context)
            titleComponent =
                if (axisStyle.getBoolean(R.styleable.Axis_showTitle, false)) {
                    axisStyle.getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.Axis_titleStyle,
                        styleableResourceId = R.styleable.TextComponentStyle,
                    ).getTextComponent(context = context)
                } else {
                    null
                }
            title = axisStyle.getString(R.styleable.Axis_title)

            when (this) {
                is VerticalAxis.Builder<*> -> {
                    horizontalLabelPosition =
                        axisStyle
                            .getInteger(R.styleable.Axis_verticalAxisHorizontalLabelPosition, 0)
                            .let { value ->
                                val values = VerticalAxis.HorizontalLabelPosition.entries
                                values[value % values.size]
                            }

                    verticalLabelPosition =
                        axisStyle
                            .getInteger(R.styleable.Axis_verticalAxisVerticalLabelPosition, 0)
                            .let { value ->
                                val values = VerticalAxis.VerticalLabelPosition.entries
                                values[value % values.size]
                            }

                    itemPlacer = axisStyle.getVerticalAxisItemPlacer()
                }

                is HorizontalAxis.Builder<*> -> {
                    itemPlacer =
                        AxisItemPlacer.Horizontal.default(
                            axisStyle.getInteger(R.styleable.Axis_horizontalAxisLabelSpacing, 1),
                            axisStyle.getInteger(R.styleable.Axis_horizontalAxisLabelOffset, 0),
                            axisStyle.getBoolean(R.styleable.Axis_shiftExtremeHorizontalAxisTicks, true),
                            axisStyle.getBoolean(R.styleable.Axis_addExtremeHorizontalAxisLabelPadding, false),
                        )
                }
            }
        }.also { axisStyle.recycle() }
    }

    private fun TypedArray.getHorizontalLayout(): HorizontalLayout =
        when (getInt(R.styleable.CartesianChartView_horizontalLayout, 0)) {
            0 -> HorizontalLayout.Segmented
            else ->
                HorizontalLayout.FullWidth(
                    getRawDimension(context, R.styleable.CartesianChartView_scalableStartContentPadding, 0f),
                    getRawDimension(context, R.styleable.CartesianChartView_scalableEndContentPadding, 0f),
                    getRawDimension(context, R.styleable.CartesianChartView_unscalableStartContentPadding, 0f),
                    getRawDimension(context, R.styleable.CartesianChartView_unscalableEndContentPadding, 0f),
                )
        }

    private fun getChart(
        cartesianChartViewTypedArray: TypedArray,
        baseTypedArray: TypedArray,
    ): CartesianChart {
        val layerFlags = cartesianChartViewTypedArray.getInt(R.styleable.CartesianChartView_layers, 0)

        val columnLayer =
            if (layerFlags.hasFlag(COLUMN_LAYER)) baseTypedArray.getColumnCartesianLayer(context) else null
        val lineLayer =
            if (layerFlags.hasFlag(LINE_LAYER)) baseTypedArray.getLineCartesianLayer(context) else null
        val candlestickLayer =
            if (layerFlags.hasFlag(CANDLESTICK_LAYER)) baseTypedArray.getCandlestickCartesianLayer(context) else null

        return CartesianChart(
            layers =
                buildList {
                    if (columnLayer != null) add(columnLayer)
                    if (lineLayer != null) add(lineLayer)
                    if (candlestickLayer != null) add(candlestickLayer)
                },
            fadingEdges = baseTypedArray.getFadingEdges(),
        ).apply {
            startAxis = baseTypedArray.getStartAxis()
            topAxis = baseTypedArray.getTopAxis()
            endAxis = baseTypedArray.getEndAxis()
            bottomAxis = baseTypedArray.getBottomAxis()
        }
    }

    private fun TypedArray.getFadingEdges(): FadingEdges? {
        val edgesLength = getRawDimension(context, R.styleable.CartesianChartView_fadingEdgeWidth, 0f)
        val startLength = getRawDimension(context, R.styleable.CartesianChartView_startFadingEdgeWidth, edgesLength)
        val endLength = getRawDimension(context, R.styleable.CartesianChartView_endFadingEdgeWidth, edgesLength)
        val threshold =
            getRawDimension(
                context,
                R.styleable.CartesianChartView_fadingEdgeVisibilityThreshold,
                FADING_EDGE_VISIBILITY_THRESHOLD_DP,
            )

        return if (startLength > 0f || endLength > 0f) {
            val interpolatorClassName = getString(R.styleable.CartesianChartView_fadingEdgeVisibilityInterpolator)

            val interpolator =
                if (interpolatorClassName != null) {
                    try {
                        context.classLoader.loadClass(interpolatorClassName).getDeclaredConstructor().newInstance()
                            as? TimeInterpolator
                    } catch (e: Exception) {
                        Log.e(
                            "ChartView",
                            "Caught exception when trying to instantiate $interpolatorClassName " +
                                "as fade interpolator.",
                        )
                        null
                    }
                } else {
                    null
                }

            FadingEdges(
                startEdgeWidthDp = startLength,
                endEdgeWidthDp = endLength,
                visibilityThresholdDp = threshold,
                visibilityInterpolator = interpolator ?: AccelerateInterpolator(),
            )
        } else {
            null
        }
    }

    private fun TypedArray.getVerticalAxisItemPlacer(): AxisItemPlacer.Vertical {
        val maxItemCount = getInteger(R.styleable.Axis_maxVerticalAxisItemCount, DEF_LABEL_COUNT)
        return AxisItemPlacer.Vertical.default(
            maxItemCount = { maxItemCount },
            shiftTopLines = getBoolean(R.styleable.Axis_shiftTopVerticalAxisLines, true),
        )
    }

    private fun TypedArray.getStartAxis(): AxisRenderer<AxisPosition.Vertical.Start>? =
        if (getBoolean(R.styleable.CartesianChartView_showStartAxis, false)) {
            getAxisBuilder(R.styleable.CartesianChartView_startAxisStyle, VerticalAxis.Builder())
                .build<AxisPosition.Vertical.Start>()
        } else {
            null
        }

    private fun TypedArray.getTopAxis(): AxisRenderer<AxisPosition.Horizontal.Top>? =
        if (getBoolean(R.styleable.CartesianChartView_showTopAxis, false)) {
            getAxisBuilder(R.styleable.CartesianChartView_topAxisStyle, HorizontalAxis.Builder())
                .build<AxisPosition.Horizontal.Top>()
        } else {
            null
        }

    private fun TypedArray.getEndAxis(): AxisRenderer<AxisPosition.Vertical.End>? =
        if (getBoolean(R.styleable.CartesianChartView_showEndAxis, false)) {
            getAxisBuilder(R.styleable.CartesianChartView_endAxisStyle, VerticalAxis.Builder())
                .build<AxisPosition.Vertical.End>()
        } else {
            null
        }

    private fun TypedArray.getBottomAxis(): AxisRenderer<AxisPosition.Horizontal.Bottom>? =
        if (getBoolean(R.styleable.CartesianChartView_showBottomAxis, false)) {
            getAxisBuilder(R.styleable.CartesianChartView_bottomAxisStyle, HorizontalAxis.Builder())
                .build<AxisPosition.Horizontal.Bottom>()
        } else {
            null
        }

    private companion object {
        const val COLUMN_LAYER = 1
        const val LINE_LAYER = 2
        const val CANDLESTICK_LAYER = 4
    }
}
