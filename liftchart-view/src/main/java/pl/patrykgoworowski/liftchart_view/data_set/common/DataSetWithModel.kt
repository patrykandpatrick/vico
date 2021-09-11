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

package pl.patrykgoworowski.liftchart_view.data_set.common

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.axis.model.MutableDataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.DataSet
import pl.patrykgoworowski.liftchart_common.data_set.renderer.RendererViewState
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener

interface DataSetWithModel<Model : EntryModel> : DataSet<Model> {
    fun draw(
        canvas: Canvas,
        rendererViewState: RendererViewState,
        segmentProperties: SegmentProperties,
        marker: Marker?,
    )

    fun addListener(listener: UpdateRequestListener)
    fun removeListener(listener: UpdateRequestListener)
    fun getEntriesModel(): Model
    fun getSegmentProperties(): SegmentProperties
    fun setToAxisModel(axisModel: MutableDataSetModel)
}
