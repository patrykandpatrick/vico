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

package pl.patrykgoworowski.vico.core.dataset.renderer

import pl.patrykgoworowski.vico.core.axis.model.MutableDataSetModel
import pl.patrykgoworowski.vico.core.dataset.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.dataset.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.dataset.threshold.ThresholdLine
import pl.patrykgoworowski.vico.core.dimensions.BoundsAware
import pl.patrykgoworowski.vico.core.layout.MeasureContext
import pl.patrykgoworowski.vico.core.marker.Marker

public interface DataSet<in Model> : BoundsAware {

    public val maxScrollAmount: Float
    public val markerLocationMap: Map<Float, MutableList<Marker.EntryModel>>

    public var minY: Float?
    public var maxY: Float?
    public var minX: Float?
    public var maxX: Float?

    public var zoom: Float?

    public fun draw(
        context: ChartDrawContext,
        model: Model,
        marker: Marker?
    )

    public fun addThresholdLine(thresholdLine: ThresholdLine): Boolean
    public fun removeThresholdLine(thresholdLine: ThresholdLine): Boolean

    public fun getMeasuredWidth(context: MeasureContext, model: Model): Int
    public fun getSegmentProperties(context: MeasureContext, model: Model): SegmentProperties
    public fun setToAxisModel(axisModel: MutableDataSetModel, model: Model)
}
