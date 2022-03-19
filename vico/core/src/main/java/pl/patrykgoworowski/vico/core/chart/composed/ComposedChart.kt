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

package pl.patrykgoworowski.vico.core.chart.composed

import pl.patrykgoworowski.vico.core.axis.model.MutableChartModel
import pl.patrykgoworowski.vico.core.chart.BaseChart
import pl.patrykgoworowski.vico.core.chart.Chart
import pl.patrykgoworowski.vico.core.chart.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.chart.segment.MutableSegmentProperties
import pl.patrykgoworowski.vico.core.chart.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.extension.set
import pl.patrykgoworowski.vico.core.extension.updateAll
import pl.patrykgoworowski.vico.core.context.MeasureContext
import pl.patrykgoworowski.vico.core.marker.Marker
import java.util.TreeMap

/**
 * The [ComposedChart] can compose multiple [Chart]s and overlay them.
 */
public class ComposedChart<Model : ChartEntryModel>(
    charts: List<Chart<Model>>
) : BaseChart<ComposedChartEntryModel<Model>>() {

    public constructor(vararg charts: Chart<Model>) : this(charts.toList())

    /**
     * The [Chart]s that make up this [ComposedChart].
     */
    public val charts: ArrayList<Chart<Model>> = ArrayList(charts)

    private val tempAxisModel = MutableChartModel()
    private val segmentProperties = MutableSegmentProperties()

    override val entryLocationMap: TreeMap<Float, MutableList<Marker.EntryModel>> = TreeMap()

    override fun setBounds(left: Number, top: Number, right: Number, bottom: Number) {
        this.bounds.set(left, top, right, bottom)
        charts.forEach { chart -> chart.setBounds(left, top, right, bottom) }
    }

    override fun drawChart(
        context: ChartDrawContext,
        model: ComposedChartEntryModel<Model>,
    ) {
        entryLocationMap.clear()
        model.forEachModelWithChart { _, item, chart ->
            chart.draw(context, item)
            entryLocationMap.updateAll(chart.entryLocationMap)
        }
    }

    override fun getSegmentProperties(
        context: MeasureContext,
        model: ComposedChartEntryModel<Model>
    ): SegmentProperties {
        segmentProperties.clear()
        model.forEachModelWithChart { _, item, chart ->
            val chartSegmentProperties = chart.getSegmentProperties(context, item)
            segmentProperties.apply {
                cellWidth = maxOf(cellWidth, chartSegmentProperties.cellWidth)
                marginWidth = maxOf(marginWidth, chartSegmentProperties.marginWidth)
            }
        }
        return segmentProperties
    }

    override fun setToChartModel(chartModel: MutableChartModel, model: ComposedChartEntryModel<Model>) {
        chartModel.clear()
        tempAxisModel.clear()
        model.forEachModelWithChart { index, item, chart ->
            chart.setToChartModel(tempAxisModel, item)
            chartModel.apply {
                minX = if (index == 0) tempAxisModel.minX else minOf(minX, tempAxisModel.minX)
                maxX = if (index == 0) tempAxisModel.maxX else maxOf(maxX, tempAxisModel.maxX)
                minY = if (index == 0) tempAxisModel.minY else minOf(minY, tempAxisModel.minY)
                maxY = if (index == 0) tempAxisModel.maxY else maxOf(maxY, tempAxisModel.maxY)
                chartModel.chartEntryModel = model
            }
        }
    }

    private inline fun ComposedChartEntryModel<Model>.forEachModelWithChart(
        action: (index: Int, item: Model, chart: Chart<Model>) -> Unit
    ) {
        val minSize = minOf(composedEntryCollections.size, charts.size)
        for (index in 0 until minSize) {
            action(index, composedEntryCollections[index], charts[index])
        }
    }
}
