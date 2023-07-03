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

package com.patrykandpatrick.vico.core.chart.composed

import com.patrykandpatrick.vico.core.chart.AXIS_VALUES_DEPRECATION_MESSAGE
import com.patrykandpatrick.vico.core.chart.BaseChart
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.ChartInsetter
import com.patrykandpatrick.vico.core.chart.insets.HorizontalInsets
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.core.extension.updateAll
import com.patrykandpatrick.vico.core.marker.Marker
import java.util.TreeMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Combines multiple [Chart]s and draws them on top of one another.
 */
public class ComposedChart<Model : ChartEntryModel>(
    charts: List<Chart<Model>>,
) : BaseChart<ComposedChartEntryModel<Model>>() {

    public constructor(vararg charts: Chart<Model>) : this(charts.toList())

    /**
     * The [Chart]s that make up this [ComposedChart].
     */
    public val charts: List<Chart<Model>> = ArrayList(charts)

    private val tempInsets = Insets()

    private val horizontalDimensions = MutableHorizontalDimensions()

    override val entryLocationMap: TreeMap<Float, MutableList<Marker.EntryModel>> = TreeMap()

    override val chartInsetters: Collection<ChartInsetter>
        get() = charts.map { it.chartInsetters }.flatten() + persistentMarkers.values

    @Deprecated(message = AXIS_VALUES_DEPRECATION_MESSAGE, level = DeprecationLevel.ERROR)
    @Suppress("DEPRECATION_ERROR")
    override var minY: Float? by childChartsValue { minY = it }

    @Deprecated(message = AXIS_VALUES_DEPRECATION_MESSAGE, level = DeprecationLevel.ERROR)
    @Suppress("DEPRECATION_ERROR")
    override var maxY: Float? by childChartsValue { maxY = it }

    @Deprecated(message = AXIS_VALUES_DEPRECATION_MESSAGE, level = DeprecationLevel.ERROR)
    @Suppress("DEPRECATION_ERROR")
    override var minX: Float? by childChartsValue { minX = it }

    @Deprecated(message = AXIS_VALUES_DEPRECATION_MESSAGE, level = DeprecationLevel.ERROR)
    @Suppress("DEPRECATION_ERROR")
    override var maxX: Float? by childChartsValue { maxX = it }

    override fun setBounds(left: Number, top: Number, right: Number, bottom: Number) {
        this.bounds.set(left, top, right, bottom)
        charts.forEach { chart -> chart.setBounds(left, top, right, bottom) }
    }

    override fun drawChart(
        context: ChartDrawContext,
        model: ComposedChartEntryModel<Model>,
    ) {
        entryLocationMap.clear()
        model.forEachModelWithChart { item, chart ->
            chart.drawScrollableContent(context, item)
            entryLocationMap.updateAll(chart.entryLocationMap)
        }
    }

    override fun drawChartInternal(context: ChartDrawContext, model: ComposedChartEntryModel<Model>) {
        drawDecorationBehindChart(context)
        if (model.entries.isNotEmpty()) {
            drawChart(context, model)
        }
    }

    override fun getHorizontalDimensions(
        context: MeasureContext,
        model: ComposedChartEntryModel<Model>,
    ): HorizontalDimensions {
        horizontalDimensions.clear()
        model.forEachModelWithChart { item, chart ->
            val chartHorizontalDimensions = chart.getHorizontalDimensions(context, item)
            horizontalDimensions.apply {
                xSpacing = maxOf(xSpacing, chartHorizontalDimensions.xSpacing)
                startPadding = maxOf(startPadding, chartHorizontalDimensions.startPadding)
                endPadding = maxOf(endPadding, chartHorizontalDimensions.endPadding)
            }
        }
        return horizontalDimensions
    }

    override fun updateChartValues(
        chartValuesManager: ChartValuesManager,
        model: ComposedChartEntryModel<Model>,
        xStep: Float?,
    ) {
        model.forEachModelWithChart { item, chart ->
            chart.updateChartValues(chartValuesManager, item, xStep)
        }
    }

    override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        horizontalDimensions: HorizontalDimensions,
    ) {
        charts.forEach { chart ->
            chart.getInsets(context, tempInsets, horizontalDimensions)
            outInsets.setValuesIfGreater(tempInsets)
        }
    }

    override fun getHorizontalInsets(context: MeasureContext, availableHeight: Float, outInsets: HorizontalInsets) {
        charts.forEach { chart ->
            chart.getHorizontalInsets(context, availableHeight, tempInsets)
            outInsets.setValuesIfGreater(start = tempInsets.start, end = tempInsets.end)
        }
    }

    private inline fun ComposedChartEntryModel<Model>.forEachModelWithChart(
        action: (item: Model, chart: Chart<Model>) -> Unit,
    ) {
        val minSize = minOf(composedEntryCollections.size, charts.size)
        for (index in 0 until minSize) {
            action(
                composedEntryCollections[index],
                charts[index],
            )
        }
    }
}

private fun childChartsValue(
    setValue: Chart<*>.(newValue: Float?) -> Unit,
): ReadWriteProperty<ComposedChart<*>, Float?> = object : ReadWriteProperty<ComposedChart<*>, Float?> {

    private var backingValue: Float? = null

    override fun getValue(thisRef: ComposedChart<*>, property: KProperty<*>): Float? = backingValue

    override fun setValue(thisRef: ComposedChart<*>, property: KProperty<*>, value: Float?) {
        thisRef.charts.forEach { chart -> chart.setValue(value) }
        backingValue = value
    }
}
