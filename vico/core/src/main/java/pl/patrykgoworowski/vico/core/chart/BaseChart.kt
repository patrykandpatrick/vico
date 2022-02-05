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

package pl.patrykgoworowski.vico.core.chart

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.chart.decoration.Decoration
import pl.patrykgoworowski.vico.core.chart.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.dimensions.BoundsAware
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.extension.getClosestMarkerEntryModel
import pl.patrykgoworowski.vico.core.extension.getEntryModel
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.model.Point

public abstract class BaseChart<in Model : ChartEntryModel> : Chart<Model>, BoundsAware {

    private val decorations = ArrayList<Decoration>()
    private val persistentMarkers = HashMap<Float, Marker>()

    protected val Model.drawMinY: Float
        get() = this@BaseChart.minY ?: minY

    protected val Model.drawMaxY: Float
        get() = this@BaseChart.maxY ?: maxY

    protected val Model.drawMinX: Float
        get() = this@BaseChart.minX ?: minX

    protected val Model.drawMaxX: Float
        get() = this@BaseChart.maxX ?: maxX

    override val bounds: RectF = RectF()

    override var minY: Float? = null
    override var maxY: Float? = null
    override var minX: Float? = null
    override var maxX: Float? = null

    override fun addDecoration(decoration: Decoration): Boolean = decorations.add(decoration)

    override fun removeDecoration(decoration: Decoration): Boolean = decorations.remove(decoration)

    override fun addPersistentMarker(x: Float, marker: Marker) {
        persistentMarkers[x] = marker
    }

    override fun removePersistentMarker(x: Float) {
        persistentMarkers.remove(x) != null
    }

    override fun draw(
        context: ChartDrawContext,
        model: Model,
        touchMarker: Marker?
    ) {
        if (model.entries.isNotEmpty()) {
            drawChart(context, model)
        }
        drawThresholdLines(context)
        persistentMarkers.forEach { (x, marker) ->
            markerLocationMap.getEntryModel(x)?.also { markerModel ->
                marker.draw(
                    context = context,
                    bounds = bounds,
                    markedEntries = markerModel,
                )
            }
        }
        val touchPoint = context.markerTouchPoint
        if (touchMarker != null && touchPoint != null) {
            drawTouchMarker(context, model, touchMarker, touchPoint)
        }
    }

    protected abstract fun drawChart(
        context: ChartDrawContext,
        model: Model,
    )

    public open fun drawTouchMarker(
        context: ChartDrawContext,
        model: Model,
        marker: Marker,
        touchPoint: Point,
    ) {
        markerLocationMap.getClosestMarkerEntryModel(touchPoint)?.also { markerModel ->
            marker.draw(
                context = context,
                bounds = bounds,
                markedEntries = markerModel,
            )
        }
    }

    private fun drawThresholdLines(context: ChartDrawContext) {
        decorations.forEach { line -> line.draw(context, bounds) }
    }
}
