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

package com.patrykandpatrick.vico.core.chart

import android.graphics.RectF
import com.patrykandpatrick.vico.core.chart.decoration.Decoration
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.ChartInsetter
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.dimensions.BoundsAware
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.getEntryModel
import com.patrykandpatrick.vico.core.extension.inClip
import com.patrykandpatrick.vico.core.extension.setAll
import com.patrykandpatrick.vico.core.marker.Marker

/**
 * A base implementation of [Chart].
 *
 * @see Chart
 */
public abstract class BaseChart<in Model : ChartEntryModel> : Chart<Model>, BoundsAware {

    private val decorations = ArrayList<Decoration>()

    private val insets: Insets = Insets()

    /**
     * A [HashMap] that links x-axis values to [Marker]s.
     */
    protected val persistentMarkers: HashMap<Float, Marker> = HashMap()

    override val bounds: RectF = RectF()

    override val chartInsetters: Collection<ChartInsetter> = persistentMarkers.values

    override var axisValuesOverrider: AxisValuesOverrider<@UnsafeVariance Model>? = null

    @Deprecated(message = AXIS_VALUES_DEPRECATION_MESSAGE, level = DeprecationLevel.ERROR)
    override var minY: Float? = null

    @Deprecated(message = AXIS_VALUES_DEPRECATION_MESSAGE, level = DeprecationLevel.ERROR)
    override var maxY: Float? = null

    @Deprecated(message = AXIS_VALUES_DEPRECATION_MESSAGE, level = DeprecationLevel.ERROR)
    override var minX: Float? = null

    @Deprecated(message = AXIS_VALUES_DEPRECATION_MESSAGE, level = DeprecationLevel.ERROR)
    override var maxX: Float? = null

    override fun addDecoration(decoration: Decoration): Boolean = decorations.add(decoration)

    override fun setDecorations(decorations: List<Decoration>) {
        this.decorations.setAll(decorations)
    }

    override fun removeDecoration(decoration: Decoration): Boolean = decorations.remove(decoration)

    override fun addPersistentMarker(x: Float, marker: Marker) {
        persistentMarkers[x] = marker
    }

    override fun setPersistentMarkers(markers: Map<Float, Marker>) {
        persistentMarkers.setAll(markers)
    }

    override fun removePersistentMarker(x: Float) {
        persistentMarkers.remove(x) != null
    }

    override fun drawScrollableContent(
        context: ChartDrawContext,
        model: Model,
    ): Unit = with(context) {
        insets.clear()
        getInsets(this, insets, horizontalDimensions)
        drawChartInternal(context, model)
    }

    override fun drawNonScrollableContent(
        context: ChartDrawContext,
        model: Model,
    ): Unit = with(context) {
        canvas.inClip(
            left = bounds.left,
            top = 0f,
            right = bounds.right,
            bottom = context.canvas.height.toFloat(),
        ) {
            drawDecorationAboveChart(context)
        }
        persistentMarkers.forEach { (x, marker) ->
            entryLocationMap.getEntryModel(x)?.also { markerModel ->
                marker.draw(
                    context = context,
                    bounds = bounds,
                    markedEntries = markerModel,
                    chartValuesProvider = chartValuesProvider,
                )
            }
        }
    }

    /**
     * An internal function that draws both [Decoration]s behind the chart and the chart itself in the clip bounds.
     */
    protected open fun drawChartInternal(
        context: ChartDrawContext,
        model: Model,
    ): Unit = with(context) {
        canvas.inClip(
            left = bounds.left - insets.getLeft(isLtr),
            top = bounds.top - insets.top,
            right = bounds.right + insets.getRight(isLtr),
            bottom = bounds.bottom + insets.bottom,
        ) {
            drawDecorationBehindChart(context)
            if (model.entries.isNotEmpty()) {
                drawChart(context, model)
            }
        }
    }

    protected abstract fun drawChart(
        context: ChartDrawContext,
        model: Model,
    )

    protected fun drawDecorationBehindChart(context: ChartDrawContext) {
        decorations.forEach { line -> line.onDrawBehindChart(context, bounds) }
    }

    protected fun drawDecorationAboveChart(context: ChartDrawContext) {
        decorations.forEach { line -> line.onDrawAboveChart(context, bounds) }
    }
}
