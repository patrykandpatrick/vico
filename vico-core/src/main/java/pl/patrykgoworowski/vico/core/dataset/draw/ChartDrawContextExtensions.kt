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

package pl.patrykgoworowski.vico.core.dataset.draw

import android.graphics.Canvas
import android.graphics.PointF
import pl.patrykgoworowski.vico.core.annotation.LongParameterListDrawFunction
import pl.patrykgoworowski.vico.core.axis.model.DataSetModel
import pl.patrykgoworowski.vico.core.dataset.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.layout.MeasureContext

@LongParameterListDrawFunction
fun chartDrawContext(
    canvas: Canvas,
    measureContext: MeasureContext,
    horizontalScroll: Float,
    markerTouchPoint: PointF?,
    segmentProperties: SegmentProperties,
    dataSetModel: DataSetModel,
) = object : ChartDrawContext {
    override val canvas: Canvas = canvas
    override val dataSetModel: DataSetModel = dataSetModel
    override val segmentProperties: SegmentProperties = segmentProperties
    override val markerTouchPoint: PointF? = markerTouchPoint
    override val horizontalScroll: Float = horizontalScroll
    override val density: Float = measureContext.density
    override val fontScale: Float = measureContext.fontScale
    override val isLtr: Boolean = measureContext.isLtr
    override val isHorizontalScrollEnabled: Boolean = measureContext.isHorizontalScrollEnabled
    override val zoom: Float = measureContext.zoom
}
