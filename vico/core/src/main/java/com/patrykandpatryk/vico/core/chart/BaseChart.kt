/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.core.chart

import android.graphics.RectF
import com.patrykandpatryk.vico.core.chart.decoration.Decoration
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.dimensions.BoundsAware
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.extension.getEntryModel
import com.patrykandpatryk.vico.core.extension.inClip
import com.patrykandpatryk.vico.core.marker.Marker

/**
 * A base implementation of [Chart].
 *
 * @see Chart
 */
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
    ): Unit = with(context) {
        canvas.inClip(bounds) {
            drawDecorationBehindChart(context)
            if (model.entries.isNotEmpty()) {
                drawChart(context, model)
            }
        }
        canvas.inClip(bounds.left, 0f, bounds.right, context.canvas.height.toFloat()) {
            drawDecorationAboveChart(context)
            persistentMarkers.forEach { (x, marker) ->
                entryLocationMap.getEntryModel(x)?.also { markerModel ->
                    marker.draw(
                        context = context,
                        bounds = bounds,
                        markedEntries = markerModel,
                    )
                }
            }
        }
    }

    protected abstract fun drawChart(
        context: ChartDrawContext,
        model: Model,
    )

    private fun drawDecorationBehindChart(context: ChartDrawContext) {
        decorations.forEach { line -> line.onDrawBehindChart(context, bounds) }
    }

    private fun drawDecorationAboveChart(context: ChartDrawContext) {
        decorations.forEach { line -> line.onDrawAboveChart(context, bounds) }
    }
}
