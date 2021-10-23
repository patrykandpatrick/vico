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

package pl.patrykgoworowski.vico.core.dataset.composed

import pl.patrykgoworowski.vico.core.axis.model.MutableDataSetModel
import pl.patrykgoworowski.vico.core.dataset.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.renderer.BaseDataSet
import pl.patrykgoworowski.vico.core.dataset.renderer.DataSet
import pl.patrykgoworowski.vico.core.dataset.segment.MutableSegmentProperties
import pl.patrykgoworowski.vico.core.dataset.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.extension.set
import pl.patrykgoworowski.vico.core.extension.updateAll
import pl.patrykgoworowski.vico.core.layout.MeasureContext
import pl.patrykgoworowski.vico.core.marker.Marker
import java.util.TreeMap

class ComposedDataSet<Model : EntryModel>(
    dataSets: List<DataSet<Model>>
) : BaseDataSet<ComposedEntryModel<Model>>() {

    constructor(vararg dataSets: DataSet<Model>) : this(dataSets.toList())

    public val dataSets = ArrayList(dataSets)

    private val tempAxisModel = MutableDataSetModel()
    private val segmentProperties = MutableSegmentProperties()

    override val markerLocationMap = TreeMap<Float, MutableList<Marker.EntryModel>>()

    override fun setBounds(left: Number, top: Number, right: Number, bottom: Number) {
        this.bounds.set(left, top, right, bottom)
        dataSets.forEach { dataSet -> dataSet.setBounds(left, top, right, bottom) }
    }

    override var zoom: Float? = null
        set(value) {
            field = value
            dataSets.forEach { dataSet -> dataSet.zoom = value }
        }

    override var maxScrollAmount: Float
        set(value) {}
        get() = dataSets.maxOf { it.maxScrollAmount }

    override fun drawDataSet(
        context: ChartDrawContext,
        model: ComposedEntryModel<Model>,
    ) {
        markerLocationMap.clear()
        model.forEachModelWithDataSet { _, item, dataSet ->
            dataSet.draw(context, item, null)
            markerLocationMap.updateAll(dataSet.markerLocationMap)
        }
    }

    override fun getMeasuredWidth(context: MeasureContext, model: ComposedEntryModel<Model>): Int {
        var result = 0
        model.forEachModelWithDataSet { _, item, dataSet ->
            result = maxOf(dataSet.getMeasuredWidth(context, item), result)
        }
        return result
    }

    override fun getSegmentProperties(
        context: MeasureContext,
        model: ComposedEntryModel<Model>
    ): SegmentProperties {
        segmentProperties.clear()
        model.forEachModelWithDataSet { _, item, dataSet ->
            val dataSetProps = dataSet.getSegmentProperties(context, item)
            segmentProperties.apply {
                cellWidth = maxOf(cellWidth, dataSetProps.cellWidth)
                marginWidth = maxOf(marginWidth, dataSetProps.marginWidth)
            }
        }
        return segmentProperties
    }

    override fun setToAxisModel(axisModel: MutableDataSetModel, model: ComposedEntryModel<Model>) {
        axisModel.clear()
        tempAxisModel.clear()
        model.forEachModelWithDataSet { index, item, dataSet ->
            dataSet.setToAxisModel(tempAxisModel, item)
            axisModel.apply {
                minX = if (index == 0) tempAxisModel.minX else minOf(minX, tempAxisModel.minX)
                maxX = if (index == 0) tempAxisModel.maxX else maxOf(maxX, tempAxisModel.maxX)
                minY = if (index == 0) tempAxisModel.minY else minOf(minY, tempAxisModel.minY)
                maxY = if (index == 0) tempAxisModel.maxY else maxOf(maxY, tempAxisModel.maxY)
                axisModel.entryModel = model
            }
        }
    }

    private inline fun ComposedEntryModel<Model>.forEachModelWithDataSet(
        action: (index: Int, item: Model, dataSet: DataSet<Model>) -> Unit
    ) {
        val minSize = minOf(composedEntryCollections.size, dataSets.size)
        for (index in 0 until minSize) {
            action(index, composedEntryCollections[index], dataSets[index])
        }
    }
}
