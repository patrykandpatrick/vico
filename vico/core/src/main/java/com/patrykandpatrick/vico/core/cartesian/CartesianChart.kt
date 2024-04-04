/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.cartesian

import android.graphics.RectF
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.AxisManager
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.core.cartesian.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.draw.CartesianChartDrawContext
import com.patrykandpatrick.vico.core.cartesian.insets.ChartInsetter
import com.patrykandpatrick.vico.core.cartesian.insets.HorizontalInsets
import com.patrykandpatrick.vico.core.cartesian.insets.Insets
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.model.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.model.CartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.values.ChartValues
import com.patrykandpatrick.vico.core.cartesian.values.MutableChartValues
import com.patrykandpatrick.vico.core.common.MutableExtraStore
import com.patrykandpatrick.vico.core.common.dimension.BoundsAware
import com.patrykandpatrick.vico.core.common.extension.inClip
import com.patrykandpatrick.vico.core.common.extension.set
import com.patrykandpatrick.vico.core.common.extension.setAll
import com.patrykandpatrick.vico.core.common.legend.Legend

/**
 * A chart based on a Cartesian coordinate plane, composed of [CartesianLayer]s.
 *
 * @property legend the legend.
 * @property fadingEdges applies a horizontal fade to the edges of the [CartesianChart], provided that it’s scrollable.
 */
public open class CartesianChart(
    layers: List<CartesianLayer<*>>,
    public var legend: Legend? = null,
    public var fadingEdges: FadingEdges? = null,
) : BoundsAware, ChartInsetter {
    private val decorations = mutableListOf<Decoration>()
    private val persistentMarkers = mutableMapOf<Float, CartesianMarker>()
    private val tempInsets = Insets()
    private val axisManager = AxisManager()
    private val virtualLayout = VirtualLayout(axisManager)
    private val _markerTargets = mutableMapOf<Float, MutableList<CartesianMarker.Target>>()

    private val drawingModelAndLayerConsumer =
        object : ModelAndLayerConsumer {
            lateinit var context: CartesianChartDrawContext

            override fun <T : CartesianLayerModel> invoke(
                model: T?,
                layer: CartesianLayer<T>,
            ) {
                layer.draw(context, model ?: return)
                layer.markerTargets.forEach { _markerTargets.getOrPut(it.key) { mutableListOf() } += it.value }
            }
        }

    private val horizontalDimensionUpdateModelAndLayerConsumer =
        object : ModelAndLayerConsumer {
            lateinit var context: CartesianMeasureContext
            lateinit var horizontalDimensions: MutableHorizontalDimensions

            override fun <T : CartesianLayerModel> invoke(
                model: T?,
                layer: CartesianLayer<T>,
            ) {
                layer.updateHorizontalDimensions(context, horizontalDimensions, model ?: return)
            }
        }

    private val chartValueUpdateModelAndLayerConsumer =
        object : ModelAndLayerConsumer {
            lateinit var chartValues: MutableChartValues

            override fun <T : CartesianLayerModel> invoke(
                model: T?,
                layer: CartesianLayer<T>,
            ) {
                layer.updateChartValues(chartValues, model ?: return)
            }
        }

    private val transformationPreparationModelAndLayerConsumer =
        object : ModelAndLayerConsumer {
            lateinit var extraStore: MutableExtraStore
            lateinit var chartValues: ChartValues

            override fun <T : CartesianLayerModel> invoke(
                model: T?,
                layer: CartesianLayer<T>,
            ) {
                layer.prepareForTransformation(model, extraStore, chartValues)
            }
        }

    /**
     * The [CartesianLayer]s of which this [CartesianChart] is composed.
     */
    public val layers: List<CartesianLayer<*>> = layers.toList()

    /**
     * The [CartesianChart]’s [ChartInsetter]s (persistent [CartesianMarker]s).
     */
    public val chartInsetters: Collection<ChartInsetter> = persistentMarkers.values

    /** Links _x_ values to [CartesianMarker.Target]s. */
    public val markerTargets: Map<Float, List<CartesianMarker.Target>> = _markerTargets

    /**
     * The start axis.
     */
    public var startAxis: Axis<AxisPosition.Vertical.Start>? by axisManager::startAxis

    /**
     * The top axis.
     */
    public var topAxis: Axis<AxisPosition.Horizontal.Top>? by axisManager::topAxis

    /**
     * The end axis.
     */
    public var endAxis: Axis<AxisPosition.Vertical.End>? by axisManager::endAxis

    /**
     * The bottom axis.
     */
    public var bottomAxis: Axis<AxisPosition.Horizontal.Bottom>? by axisManager::bottomAxis

    override val bounds: RectF = RectF()

    public constructor(vararg layers: CartesianLayer<*>, legend: Legend? = null, fadingEdges: FadingEdges? = null) :
        this(layers.toList(), legend, fadingEdges)

    override fun setBounds(
        left: Number,
        top: Number,
        right: Number,
        bottom: Number,
    ) {
        bounds.set(left, top, right, bottom)
        layers.forEach { it.setBounds(left, top, right, bottom) }
    }

    /**
     * Prepares the [CartesianChart] for drawing.
     */
    public fun prepare(
        context: CartesianMeasureContext,
        model: CartesianChartModel,
        horizontalDimensions: MutableHorizontalDimensions,
        bounds: RectF,
        marker: CartesianMarker?,
    ) {
        _markerTargets.clear()
        model.forEachWithLayer(
            horizontalDimensionUpdateModelAndLayerConsumer.apply {
                this.context = context
                this.horizontalDimensions = horizontalDimensions
            },
        )
        startAxis?.updateHorizontalDimensions(context, horizontalDimensions)
        topAxis?.updateHorizontalDimensions(context, horizontalDimensions)
        endAxis?.updateHorizontalDimensions(context, horizontalDimensions)
        bottomAxis?.updateHorizontalDimensions(context, horizontalDimensions)
        virtualLayout.setBounds(context, bounds, this, legend, horizontalDimensions, marker)
    }

    /**
     * Draws the [CartesianChart].
     */
    public fun draw(
        context: CartesianChartDrawContext,
        model: CartesianChartModel,
    ) {
        val canvasSaveCount = if (fadingEdges != null) context.saveLayer() else -1
        axisManager.drawBehindChart(context)
        decorations.forEach { it.onDrawBehindChart(context, bounds) }
        model.forEachWithLayer(drawingModelAndLayerConsumer.apply { this.context = context })
        fadingEdges?.run {
            applyFadingEdges(context, bounds)
            context.restoreCanvasToCount(canvasSaveCount)
        }
        axisManager.drawAboveChart(context)
        context.canvas.inClip(bounds.left, 0f, bounds.right, context.canvas.height.toFloat()) {
            decorations.forEach { it.onDrawAboveChart(context, bounds) }
        }
        persistentMarkers.forEach { (x, marker) -> markerTargets[x]?.let { targets -> marker.draw(context, targets) } }
        legend?.draw(context, bounds)
    }

    /**
     * Updates [chartValues] in accordance with [model].
     */
    public fun updateChartValues(
        chartValues: MutableChartValues,
        model: CartesianChartModel,
        xStep: Float?,
    ) {
        chartValues.update(xStep ?: model.getXDeltaGcd(), model)
        model.forEachWithLayer(chartValueUpdateModelAndLayerConsumer.apply { this.chartValues = chartValues })
    }

    override fun getInsets(
        context: CartesianMeasureContext,
        outInsets: Insets,
        horizontalDimensions: HorizontalDimensions,
    ) {
        tempInsets.clear()
        layers.forEach { it.getInsets(context, tempInsets, horizontalDimensions) }
        outInsets.setValuesIfGreater(tempInsets)
    }

    override fun getHorizontalInsets(
        context: CartesianMeasureContext,
        availableHeight: Float,
        outInsets: HorizontalInsets,
    ) {
        tempInsets.clear()
        layers.forEach { it.getHorizontalInsets(context, availableHeight, tempInsets) }
        outInsets.setValuesIfGreater(start = tempInsets.start, end = tempInsets.end)
    }

    /**
     * Prepares the [CartesianLayer]s for a difference animation.
     */
    public fun prepareForTransformation(
        model: CartesianChartModel?,
        extraStore: MutableExtraStore,
        chartValues: ChartValues,
    ) {
        model
            ?.forEachWithLayer(
                transformationPreparationModelAndLayerConsumer.apply {
                    this.extraStore = extraStore
                    this.chartValues = chartValues
                },
            )
            ?: layers.forEach { it.prepareForTransformation(null, extraStore, chartValues) }
    }

    /**
     * Carries out the pending difference animation.
     */
    public suspend fun transform(
        extraStore: MutableExtraStore,
        fraction: Float,
    ) {
        layers.forEach { it.transform(extraStore, fraction) }
    }

    /**
     * Adds the provided [Decoration].
     */
    public fun addDecoration(decoration: Decoration) {
        decorations.add(decoration)
    }

    /**
     * Removes all [Decoration]s and adds the provided ones.
     */
    public fun setDecorations(decorations: List<Decoration>) {
        this.decorations.setAll(decorations)
    }

    /**
     * Removes the specified [Decoration], provided that it’s present.
     */
    public fun removeDecoration(decoration: Decoration): Boolean = decorations.remove(decoration)

    /**
     * Adds the provided persistent [CartesianMarker] at [x].
     */
    public fun addPersistentMarker(
        x: Float,
        marker: CartesianMarker,
    ) {
        persistentMarkers[x] = marker
    }

    /**
     * Removes all persistent [CartesianMarker]s and adds the provided ones.
     */
    public fun setPersistentMarkers(markers: Map<Float, CartesianMarker>) {
        persistentMarkers.setAll(markers)
    }

    /**
     * Removes the persistent [CartesianMarker] at [x], provided that there is such a [CartesianMarker].
     */
    public fun removePersistentMarker(x: Float): Boolean = persistentMarkers.remove(x) != null

    protected fun CartesianChartModel.forEachWithLayer(consumer: ModelAndLayerConsumer) {
        val freeModels = models.toMutableList()
        layers.forEach { layer ->
            when (layer) {
                is ColumnCartesianLayer -> freeModels.consume(layer, consumer)
                is LineCartesianLayer -> freeModels.consume(layer, consumer)
                is CandlestickCartesianLayer -> freeModels.consume(layer, consumer)
                else -> throw IllegalArgumentException("Unexpected `CartesianLayer` implementation.")
            }
        }
    }

    private inline fun <reified T : CartesianLayerModel> MutableList<CartesianLayerModel>.consume(
        layer: CartesianLayer<T>,
        consumer: ModelAndLayerConsumer,
    ) {
        val model = filterIsInstance<T>().firstOrNull()
        consumer(model, layer)
        if (model != null) remove(model)
    }

    protected interface ModelAndLayerConsumer {
        public operator fun <T : CartesianLayerModel> invoke(
            model: T?,
            layer: CartesianLayer<T>,
        )
    }
}
