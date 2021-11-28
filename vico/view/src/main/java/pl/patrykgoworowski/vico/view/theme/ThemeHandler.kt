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
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.axis.Axis
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.axisBuilder
import pl.patrykgoworowski.vico.core.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.dataset.column.ColumnDataSet
import pl.patrykgoworowski.vico.core.dataset.composed.ComposedDataSet
import pl.patrykgoworowski.vico.core.dataset.composed.ComposedEntryModel
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.renderer.DataSet
import pl.patrykgoworowski.vico.core.extension.hasFlag
import pl.patrykgoworowski.vico.view.R

internal class ThemeHandler(
    private val context: Context,
    attrs: AttributeSet?,
    chartType: ChartType
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

    public var chart: DataSet<EntryModel>? = null
        private set

    public var composedChart: DataSet<ComposedEntryModel<EntryModel>>? = null
        private set

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BaseChartView).use { typedArray ->
            if (typedArray.getBoolean(R.styleable.BaseChartView_showStartAxis, false)) {
                startAxis = VerticalAxis.Builder(typedArray.getAxis()).build()
            }
            if (typedArray.getBoolean(R.styleable.BaseChartView_showTopAxis, false)) {
                topAxis = HorizontalAxis.Builder(typedArray.getAxis()).build()
            }
            if (typedArray.getBoolean(R.styleable.BaseChartView_showEndAxis, false)) {
                endAxis = VerticalAxis.Builder(typedArray.getAxis()).build()
            }
            if (typedArray.getBoolean(R.styleable.BaseChartView_showBottomAxis, false)) {
                bottomAxis = HorizontalAxis.Builder(typedArray.getAxis()).build()
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

    private fun TypedArray.getAxis(): Axis.Builder {

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
                    defaultValue = Dimens.AXIS_TICK_LENGTH,
                )
                guideline = axisStyle.getLineComponent(
                    resourceId = R.styleable.Axis_axisGuidelineStyle,
                    styleableResourceId = R.styleable.LineComponentStyle,
                    defaultShape = Shapes.dashedShape(),
                )
                label = axisStyle.getNestedTypedArray(
                    context = context,
                    resourceId = R.styleable.Axis_axisLabelStyle,
                    styleableResourceId = R.styleable.LabelStyle
                ).getTextComponent(context = context)
            }
        }
    }

    private fun TypedArray.getChart(): DataSet<EntryModel>? {
        val chartFlags = getInt(R.styleable.ChartView_chartType, 0)

        return when {
            chartFlags.hasFlag(Flags.COLUMN_CHART) -> getColumnChart(context)
            chartFlags.hasFlag(Flags.LINE_CHART) -> getLineChart(context)
            else -> null
        }
    }

    private fun TypedArray.getComposedChart(): DataSet<ComposedEntryModel<EntryModel>>? {
        val chartFlags = getInt(R.styleable.ComposedChartView_charts, 0)

        val columnDataSet: ColumnDataSet? = if (chartFlags.hasFlag(Flags.COLUMN_CHART)) {
            getColumnChart(context)
        } else {
            null
        }
        val lineDataSet = if (chartFlags.hasFlag(Flags.LINE_CHART)) {
            getLineChart(context)
        } else {
            null
        }

        return when {
            columnDataSet != null && lineDataSet != null ->
                ComposedDataSet(columnDataSet, lineDataSet)
            columnDataSet != null ->
                columnDataSet
            lineDataSet != null ->
                lineDataSet
            else -> null
        }
    }

    internal enum class ChartType {
        Single,
        Composed,
        Unknown,
    }

    private companion object Flags {
        const val COLUMN_CHART = 1
        const val LINE_CHART = 2
    }
}
