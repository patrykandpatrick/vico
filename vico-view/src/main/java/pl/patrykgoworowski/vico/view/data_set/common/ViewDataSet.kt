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

package pl.patrykgoworowski.vico.view.dataset.common

import android.graphics.Canvas
import pl.patrykgoworowski.vico.core.axis.model.MutableDataSetModel
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.renderer.DataSet
import pl.patrykgoworowski.vico.core.dataset.renderer.RendererViewState
import pl.patrykgoworowski.vico.core.dataset.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.extension.runEach
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.view.common.UpdateRequestListener

class ViewDataSet<Model : EntryModel>(
    private val dataSet: DataSet<Model>,
    private var model: Model,
) : DataSetWithModel<Model>, DataSet<Model> by dataSet {

    private val listeners = ArrayList<UpdateRequestListener>()

    public fun setModel(model: Model) {
        this.model = model
        listeners.runEach()
    }

    override fun getEntriesModel(): Model = model

    override fun getSegmentProperties(): SegmentProperties = dataSet.getSegmentProperties(model)

    override fun setToAxisModel(axisModel: MutableDataSetModel) {
        dataSet.setToAxisModel(axisModel, model)
    }

    override fun draw(
        canvas: Canvas,
        rendererViewState: RendererViewState,
        segmentProperties: SegmentProperties,
        marker: Marker?,
    ) {
        dataSet.draw(canvas, model, segmentProperties, rendererViewState, marker)
    }

    override fun addListener(listener: UpdateRequestListener) {
        listeners += listener
    }

    override fun removeListener(listener: UpdateRequestListener) {
        listeners -= listener
    }
}
