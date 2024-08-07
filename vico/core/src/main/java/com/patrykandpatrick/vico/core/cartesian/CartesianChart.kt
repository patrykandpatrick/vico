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
import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.cartesian.CartesianChart.PersistentMarkerScope
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.AxisManager
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.ChartValues
import com.patrykandpatrick.vico.core.cartesian.data.MutableChartValues
import com.patrykandpatrick.vico.core.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.common.Legend
import com.patrykandpatrick.vico.core.common.Point
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore
import com.patrykandpatrick.vico.core.common.orZero
import com.patrykandpatrick.vico.core.common.saveLayer
import java.util.Objects
import java.util.SortedMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.abs

/**
 * A chart based on a Cartesian coordinate plane, composed of [CartesianLayer]s.
 *
 * @param startAxis the start [Axis].
 * @param topAxis the top [Axis].
 * @param endAxis the end [Axis].
 * @param bottomAxis the bottom [Axis].
 * @property layers the [CartesianLayer]s.
 * @property marker appears when the [CartesianChart] is tapped.
 * @property markerVisibilityListener allows for listening to [marker] visibility changes.
 * @property horizontalLayout defines how the [CartesianChart]’s content is positioned horizontally.
 * @property legend the legend.
 * @property fadingEdges applies a horizontal fade to the edges of the [CartesianChart], provided
 *   that it’s scrollable.
 * @property decorations the [Decoration]s.
 * @property persistentMarkers adds persistent [CartesianMarker]s.
 * @property getXStep defines the _x_ step (the difference between the _x_ values of neighboring
 *   major entries).
 */
public open class CartesianChart(
  vararg layers: CartesianLayer<*>,
  startAxis: Axis<Axis.Position.Vertical.Start>? = null,
  topAxis: Axis<Axis.Position.Horizontal.Top>? = null,
  endAxis: Axis<Axis.Position.Vertical.End>? = null,
  bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? = null,
  public var marker: CartesianMarker? = null,
  public var markerVisibilityListener: CartesianMarkerVisibilityListener? = null,
  public var horizontalLayout: HorizontalLayout = HorizontalLayout.Segmented,
  public var legend: Legend<CartesianMeasuringContext, CartesianDrawingContext>? = null,
  public var fadingEdges: FadingEdges? = null,
  public var decorations: List<Decoration> = emptyList(),
  public var persistentMarkers: (PersistentMarkerScope.(ExtraStore) -> Unit)? = null,
  public var getXStep: ((CartesianChartModel) -> Double) = { it.getXDeltaGcd() },
) : ChartInsetter<CartesianChartModel> {
  private val persistentMarkerMap = mutableMapOf<Double, CartesianMarker>()
  private val persistentMarkerScope = PersistentMarkerScope {
    persistentMarkerMap[it.toDouble()] = this
  }
  private var previousPersistentMarkerHashCode: Int? = null
  private val insets = Insets()
  private val axisManager = AxisManager()
  private val _markerTargets = sortedMapOf<Double, MutableList<CartesianMarker.Target>>()
  private var previousMarkerTargetHashCode: Int? = null

  private val drawingModelAndLayerConsumer =
    object : ModelAndLayerConsumer {
      lateinit var context: CartesianDrawingContext

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        layer.draw(context, model ?: return)
        layer.markerTargets.forEach {
          _markerTargets.getOrPut(it.key) { mutableListOf() } += it.value
        }
      }
    }

  private val horizontalDimensionUpdateModelAndLayerConsumer =
    object : ModelAndLayerConsumer {
      lateinit var context: CartesianMeasuringContext
      lateinit var horizontalDimensions: MutableHorizontalDimensions

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        layer.updateHorizontalDimensions(context, horizontalDimensions, model ?: return)
      }
    }

  private val chartValueUpdateModelAndLayerConsumer =
    object : ModelAndLayerConsumer {
      lateinit var chartValues: MutableChartValues

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        layer.updateChartValues(chartValues, model ?: return)
      }
    }

  private val insetUpdateModelAndLayerConsumer =
    object : ModelAndLayerConsumer {
      lateinit var context: CartesianMeasuringContext
      lateinit var horizontalDimensions: HorizontalDimensions
      lateinit var insets: Insets

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        layer.updateInsets(context, horizontalDimensions, model ?: return, insets)
      }
    }

  private val horizontalInsetUpdateModelAndLayerConsumer =
    object : ModelAndLayerConsumer {
      lateinit var context: CartesianMeasuringContext
      var freeHeight: Float = 0f
      lateinit var insets: HorizontalInsets

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        layer.updateHorizontalInsets(context, freeHeight, model ?: return, insets)
      }
    }

  private val transformationPreparationModelAndLayerConsumer =
    object : ModelAndLayerConsumer {
      lateinit var extraStore: MutableExtraStore
      lateinit var chartValues: ChartValues

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        layer.prepareForTransformation(model, extraStore, chartValues)
      }
    }

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) public val layerBounds: RectF = RectF()

  /** The [CartesianLayer]s of which this [CartesianChart] is composed. */
  public val layers: List<CartesianLayer<*>> = layers.toList()

  /** Links _x_ values to [CartesianMarker.Target]s. */
  @Suppress("UNCHECKED_CAST")
  public val markerTargets: SortedMap<Double, List<CartesianMarker.Target>> =
    _markerTargets as SortedMap<Double, List<CartesianMarker.Target>>

  /** The start [Axis]. */
  public var startAxis: Axis<Axis.Position.Vertical.Start>? by axisManager::startAxis

  /** The top [Axis]. */
  public var topAxis: Axis<Axis.Position.Horizontal.Top>? by axisManager::topAxis

  /** The end [Axis]. */
  public var endAxis: Axis<Axis.Position.Vertical.End>? by axisManager::endAxis

  /** The bottom [Axis]. */
  public var bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? by axisManager::bottomAxis

  init {
    axisManager.startAxis = startAxis
    axisManager.topAxis = topAxis
    axisManager.endAxis = endAxis
    axisManager.bottomAxis = bottomAxis
  }

  private fun setLayerBounds(left: Float, top: Float, right: Float, bottom: Float) {
    layerBounds.set(left, top, right, bottom)
  }

  /** Prepares the [CartesianChart] for drawing. */
  public fun prepare(
    context: CartesianMeasuringContext,
    model: CartesianChartModel,
    horizontalDimensions: MutableHorizontalDimensions,
    canvasBounds: RectF,
  ) {
    _markerTargets.clear()
    insets.clear()
    val persistentMarkerHashCode = Objects.hash(persistentMarkers, model.extraStore)
    if (persistentMarkerHashCode != previousPersistentMarkerHashCode) {
      persistentMarkerMap.clear()
      persistentMarkers?.invoke(persistentMarkerScope, model.extraStore)
      previousPersistentMarkerHashCode = persistentMarkerHashCode
    }
    model.forEachWithLayer(
      horizontalDimensionUpdateModelAndLayerConsumer.apply {
        this.context = context
        this.horizontalDimensions = horizontalDimensions
      }
    )
    startAxis?.updateHorizontalDimensions(context, horizontalDimensions)
    topAxis?.updateHorizontalDimensions(context, horizontalDimensions)
    endAxis?.updateHorizontalDimensions(context, horizontalDimensions)
    bottomAxis?.updateHorizontalDimensions(context, horizontalDimensions)
    val insetters = buildList {
      add(this@CartesianChart)
      addAll(axisManager.axisCache)
      marker?.let(::add)
      addAll(persistentMarkerMap.values)
    }
    insetters.forEach { it.updateInsets(context, horizontalDimensions, model, insets) }
    val legendHeight = legend?.getHeight(context, canvasBounds.width()).orZero
    val freeHeight = canvasBounds.height() - insets.vertical - legendHeight
    insetters.forEach { it.updateHorizontalInsets(context, freeHeight, model, insets) }
    setLayerBounds(
      canvasBounds.left + insets.getLeft(context.isLtr),
      canvasBounds.top + insets.top,
      canvasBounds.right - insets.getRight(context.isLtr),
      canvasBounds.bottom - insets.bottom - legendHeight,
    )
    axisManager.setAxesBounds(context, canvasBounds, layerBounds, insets)
    legend?.setBounds(
      left = canvasBounds.left,
      top = layerBounds.bottom + insets.bottom,
      right = canvasBounds.right,
      bottom = layerBounds.bottom + insets.bottom + legendHeight,
    )
  }

  /** Draws the [CartesianChart]. */
  public fun draw(
    context: CartesianDrawingContext,
    model: CartesianChartModel,
    pointerPosition: Point?,
  ) {
    val canvasSaveCount = if (fadingEdges != null) context.canvas.saveLayer() else -1
    axisManager.drawUnderLayers(context)
    decorations.forEach { it.drawUnderLayers(context) }
    model.forEachWithLayer(drawingModelAndLayerConsumer.apply { this.context = context })
    fadingEdges?.run {
      draw(context)
      context.canvas.restoreToCount(canvasSaveCount)
    }
    axisManager.drawOverLayers(context)
    decorations.forEach { it.drawOverLayers(context) }
    persistentMarkerMap.forEach { (x, marker) ->
      markerTargets[x]?.let { targets -> marker.draw(context, targets) }
    }
    legend?.draw(context)
    drawMarker(context, pointerPosition)
  }

  /** Updates [chartValues] in accordance with [model]. */
  public fun updateChartValues(chartValues: MutableChartValues, model: CartesianChartModel) {
    chartValues.update(getXStep(model), model)
    model.forEachWithLayer(
      chartValueUpdateModelAndLayerConsumer.apply { this.chartValues = chartValues }
    )
  }

  override fun updateInsets(
    context: CartesianMeasuringContext,
    horizontalDimensions: HorizontalDimensions,
    model: CartesianChartModel,
    insets: Insets,
  ) {
    model.forEachWithLayer(
      insetUpdateModelAndLayerConsumer.apply {
        this.context = context
        this.horizontalDimensions = horizontalDimensions
        this.insets = insets
      }
    )
  }

  override fun updateHorizontalInsets(
    context: CartesianMeasuringContext,
    freeHeight: Float,
    model: CartesianChartModel,
    insets: HorizontalInsets,
  ) {
    model.forEachWithLayer(
      horizontalInsetUpdateModelAndLayerConsumer.apply {
        this.context = context
        this.freeHeight = freeHeight
        this.insets = insets
      }
    )
  }

  /** Prepares the [CartesianLayer]s for a difference animation. */
  public fun prepareForTransformation(
    model: CartesianChartModel?,
    extraStore: MutableExtraStore,
    chartValues: ChartValues,
  ) {
    model?.forEachWithLayer(
      transformationPreparationModelAndLayerConsumer.apply {
        this.extraStore = extraStore
        this.chartValues = chartValues
      }
    ) ?: layers.forEach { it.prepareForTransformation(null, extraStore, chartValues) }
  }

  /** Carries out the pending difference animation. */
  public suspend fun transform(extraStore: MutableExtraStore, fraction: Float) {
    layers.forEach { it.transform(extraStore, fraction) }
  }

  protected open fun CartesianChartModel.forEachWithLayer(consumer: ModelAndLayerConsumer) {
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

  protected open fun drawMarker(context: CartesianDrawingContext, pointerPosition: Point?) {
    val marker = marker ?: return
    if (pointerPosition == null || markerTargets.isEmpty()) {
      if (previousMarkerTargetHashCode != null) markerVisibilityListener?.onHidden(marker)
      previousMarkerTargetHashCode = null
      return
    }
    var targets = emptyList<CartesianMarker.Target>()
    var previousDistance = Float.POSITIVE_INFINITY
    for (xTargets in markerTargets.values) {
      val (distance, canvasXTargets) =
        xTargets.groupBy { abs(pointerPosition.x - it.canvasX) }.minBy { it.key }
      if (distance > previousDistance) break
      targets = canvasXTargets
      previousDistance = distance
    }
    marker.draw(context, targets)
    val targetHashCode = targets.hashCode()
    if (previousMarkerTargetHashCode == null) {
      markerVisibilityListener?.onShown(marker, targets)
    } else if (targetHashCode != previousMarkerTargetHashCode) {
      markerVisibilityListener?.onUpdated(marker, targets)
    }
    previousMarkerTargetHashCode = targetHashCode
  }

  protected inline fun <reified T : CartesianLayerModel> MutableList<CartesianLayerModel>.consume(
    layer: CartesianLayer<T>,
    consumer: ModelAndLayerConsumer,
  ) {
    val model = filterIsInstance<T>().firstOrNull()
    consumer(model, layer)
    if (model != null) remove(model)
  }

  protected interface ModelAndLayerConsumer {
    public operator fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>)
  }

  /** Facilitates adding persistent [CartesianMarker]s to [CartesianChart]s. */
  public fun interface PersistentMarkerScope {
    /** Adds this [CartesianMarker] at [x]. */
    public infix fun CartesianMarker.at(x: Number)
  }
}
