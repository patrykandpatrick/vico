/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

import android.graphics.Canvas
import android.graphics.RectF
import androidx.annotation.RestrictTo
import androidx.compose.runtime.Stable
import com.patrykandpatrick.vico.core.cartesian.CartesianChart.PersistentMarkerScope
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.AxisManager
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerMarginUpdater
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.HorizontalCartesianLayerMargins
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.common.Legend
import com.patrykandpatrick.vico.core.common.Point
import com.patrykandpatrick.vico.core.common.data.CacheStore
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore
import com.patrykandpatrick.vico.core.common.getBitmap
import com.patrykandpatrick.vico.core.common.orZero
import com.patrykandpatrick.vico.core.common.saveLayer
import java.util.Objects
import java.util.SortedMap
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.abs

/** A chart based on a Cartesian coordinate plane, composed of [CartesianLayer]s. */
@Stable
public open class CartesianChart
private constructor(
  vararg layers: CartesianLayer<*>,
  startAxis: Axis<Axis.Position.Vertical.Start>? = null,
  topAxis: Axis<Axis.Position.Horizontal.Top>? = null,
  endAxis: Axis<Axis.Position.Vertical.End>? = null,
  bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? = null,
  /** @suppress */
  @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) public val marker: CartesianMarker? = null,
  protected val markerVisibilityListener: CartesianMarkerVisibilityListener? = null,
  /** @suppress */
  @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public val layerPadding: ((ExtraStore) -> CartesianLayerPadding) = { CartesianLayerPadding() },
  protected val legend: Legend<CartesianMeasuringContext, CartesianDrawingContext>? = null,
  protected val fadingEdges: FadingEdges? = null,
  protected val decorations: List<Decoration> = emptyList(),
  protected val persistentMarkers: (PersistentMarkerScope.(ExtraStore) -> Unit)? = null,
  protected val getXStep: ((CartesianChartModel) -> Double) = { it.getXDeltaGcd() },
  /** @suppress */
  @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) public val id: UUID,
  private var previousMarkerTargetHashCode: Int?,
  private val persistentMarkerMap: MutableMap<Double, CartesianMarker>,
  private var previousPersistentMarkerHashCode: Int?,
) : CartesianLayerMarginUpdater<CartesianChartModel> {
  private val persistentMarkerScope = PersistentMarkerScope {
    persistentMarkerMap[it.toDouble()] = this
  }
  private val layerMargins = CartesianLayerMargins()
  private val layerCanvas = Canvas()
  private val axisManager = AxisManager()
  private val _markerTargets = sortedMapOf<Double, MutableList<CartesianMarker.Target>>()

  private val drawingConsumer =
    object : ModelAndLayerConsumer {
      lateinit var context: CartesianDrawingContext

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        layer.draw(context, model ?: return)
        layer.markerTargets.forEach {
          _markerTargets.getOrPut(it.key) { mutableListOf() } += it.value
        }
      }
    }

  private val layerDimensionUpdateConsumer =
    object : ModelAndLayerConsumer {
      lateinit var context: CartesianMeasuringContext
      lateinit var layerDimensions: MutableCartesianLayerDimensions

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        layer.updateDimensions(context, layerDimensions, model ?: return)
      }
    }

  private val rangeUpdateConsumer =
    object : ModelAndLayerConsumer {
      lateinit var ranges: MutableCartesianChartRanges

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        layer.updateChartRanges(ranges, model ?: return)
      }
    }

  private val layerMarginUpdateConsumer =
    object : ModelAndLayerConsumer {
      lateinit var context: CartesianMeasuringContext
      lateinit var layerDimensions: CartesianLayerDimensions
      lateinit var layerMargins: CartesianLayerMargins

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        layer.updateLayerMargins(context, layerMargins, layerDimensions, model ?: return)
      }
    }

  private val horizontalLayerMarginUpdateConsumer =
    object : ModelAndLayerConsumer {
      lateinit var context: CartesianMeasuringContext
      lateinit var horizontalLayerMargins: HorizontalCartesianLayerMargins
      var layerHeight: Float = 0f

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        layer.updateHorizontalLayerMargins(
          context,
          horizontalLayerMargins,
          layerHeight,
          model ?: return,
        )
      }
    }

  private val transformationPreparationConsumer =
    object : ModelAndLayerConsumer {
      lateinit var extraStore: MutableExtraStore
      lateinit var ranges: CartesianChartRanges

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        layer.prepareForTransformation(model, ranges, extraStore)
      }
    }

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) public val layerBounds: RectF = RectF()

  /** The [CartesianLayer]s of which this [CartesianChart] is composed. */
  public val layers: List<CartesianLayer<*>> = layers.toList()

  /** Links _x_ values to [CartesianMarker.Target]s. */
  @Suppress("UNCHECKED_CAST")
  protected val markerTargets: SortedMap<Double, List<CartesianMarker.Target>> =
    _markerTargets as SortedMap<Double, List<CartesianMarker.Target>>

  /** The start [Axis]. */
  public val startAxis: Axis<Axis.Position.Vertical.Start>? by axisManager::startAxis

  /** The top [Axis]. */
  public val topAxis: Axis<Axis.Position.Horizontal.Top>? by axisManager::topAxis

  /** The end [Axis]. */
  public val endAxis: Axis<Axis.Position.Vertical.End>? by axisManager::endAxis

  /** The bottom [Axis]. */
  public val bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? by axisManager::bottomAxis

  init {
    axisManager.startAxis = startAxis
    axisManager.topAxis = topAxis
    axisManager.endAxis = endAxis
    axisManager.bottomAxis = bottomAxis
  }

  /**
   * Creates a [CartesianChart].
   *
   * @param layers the [CartesianLayer]s.
   * @param startAxis the start [Axis].
   * @param topAxis the top [Axis].
   * @param endAxis the end [Axis].
   * @param bottomAxis the bottom [Axis].
   * @param marker appears when the [CartesianChart] is tapped.
   * @param markerVisibilityListener allows for listening to [marker] visibility changes.
   * @param layerPadding returns the [CartesianLayerPadding].
   * @param legend the legend.
   * @param fadingEdges applies a horizontal fade to the edges of the [CartesianChart], provided
   *   that it’s scrollable.
   * @param decorations the [Decoration]s.
   * @param persistentMarkers adds persistent [CartesianMarker]s.
   * @param getXStep defines the _x_ step (the difference between neighboring major _x_ values).
   */
  public constructor(
    vararg layers: CartesianLayer<*>,
    startAxis: Axis<Axis.Position.Vertical.Start>? = null,
    topAxis: Axis<Axis.Position.Horizontal.Top>? = null,
    endAxis: Axis<Axis.Position.Vertical.End>? = null,
    bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? = null,
    marker: CartesianMarker? = null,
    markerVisibilityListener: CartesianMarkerVisibilityListener? = null,
    layerPadding: ((ExtraStore) -> CartesianLayerPadding) = { CartesianLayerPadding() },
    legend: Legend<CartesianMeasuringContext, CartesianDrawingContext>? = null,
    fadingEdges: FadingEdges? = null,
    decorations: List<Decoration> = emptyList(),
    persistentMarkers: (PersistentMarkerScope.(ExtraStore) -> Unit)? = null,
    getXStep: ((CartesianChartModel) -> Double) = { it.getXDeltaGcd() },
  ) : this(
    layers = layers,
    startAxis = startAxis,
    topAxis = topAxis,
    endAxis = endAxis,
    bottomAxis = bottomAxis,
    marker = marker,
    markerVisibilityListener = markerVisibilityListener,
    layerPadding = layerPadding,
    legend = legend,
    fadingEdges = fadingEdges,
    decorations = decorations,
    persistentMarkers = persistentMarkers,
    getXStep = getXStep,
    id = UUID.randomUUID(),
    previousMarkerTargetHashCode = null,
    persistentMarkerMap = mutableMapOf(),
    previousPersistentMarkerHashCode = null,
  )

  private fun setLayerBounds(left: Float, top: Float, right: Float, bottom: Float) {
    layerBounds.set(left, top, right, bottom)
  }

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public fun prepare(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
  ) {
    with(context) {
      _markerTargets.clear()
      layerMargins.clear()
      val persistentMarkerHashCode = Objects.hash(persistentMarkers, model.extraStore)
      if (persistentMarkerHashCode != previousPersistentMarkerHashCode) {
        updatePersistentMarkers(model.extraStore)
        previousPersistentMarkerHashCode = persistentMarkerHashCode
      }
      model.forEachWithLayer(
        layerDimensionUpdateConsumer.apply {
          this.context = context
          this.layerDimensions = layerDimensions
        }
      )
      startAxis?.updateLayerDimensions(context, layerDimensions)
      topAxis?.updateLayerDimensions(context, layerDimensions)
      endAxis?.updateLayerDimensions(context, layerDimensions)
      bottomAxis?.updateLayerDimensions(context, layerDimensions)
      val marginUpdaters = buildList {
        add(this@CartesianChart)
        addAll(axisManager.axisCache)
        marker?.let(::add)
        addAll(persistentMarkerMap.values)
      }
      marginUpdaters.forEach { updater ->
        updater.updateLayerMargins(context, layerMargins, layerDimensions, model)
      }
      val legendHeight = legend?.getHeight(context, canvasBounds.width()).orZero
      val freeHeight = canvasBounds.height() - layerMargins.vertical - legendHeight
      marginUpdaters.forEach { updater ->
        updater.updateHorizontalLayerMargins(context, layerMargins, freeHeight, model)
      }
      setLayerBounds(
        canvasBounds.left + layerMargins.getLeft(isLtr),
        canvasBounds.top + layerMargins.top,
        canvasBounds.right - layerMargins.getRight(isLtr),
        canvasBounds.bottom - layerMargins.bottom - legendHeight,
      )
      axisManager.setAxesBounds(context, canvasBounds, layerBounds, layerMargins)
      legend?.setBounds(
        left = canvasBounds.left,
        top = layerBounds.bottom + layerMargins.bottom,
        right = canvasBounds.right,
        bottom = layerBounds.bottom + layerMargins.bottom + legendHeight,
      )
    }
  }

  private fun updatePersistentMarkers(extraStore: ExtraStore) {
    persistentMarkerMap.clear()
    persistentMarkers?.invoke(persistentMarkerScope, extraStore)
  }

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public fun draw(context: CartesianDrawingContext) {
    with(context) {
      val canvasSaveCount = if (fadingEdges != null) canvas.saveLayer() else -1
      axisManager.drawUnderLayers(context)
      decorations.forEach { it.drawUnderLayers(context) }
      val layerBitmap = getBitmap(cacheKeyNamespace)
      layerCanvas.setBitmap(layerBitmap)
      withCanvas(layerCanvas) {
        model.forEachWithLayer(drawingConsumer.apply { this.context = context })
      }
      forEachPersistentMarker { marker, targets -> marker.drawUnderLayers(context, targets) }
      val markerTargets = getMarkerTargets(context, pointerPosition)
      if (markerTargets.isNotEmpty()) marker?.drawUnderLayers(context, markerTargets)
      canvas.drawBitmap(layerBitmap, 0f, 0f, null)
      fadingEdges?.run {
        draw(context)
        canvas.restoreToCount(canvasSaveCount)
      }
      axisManager.drawOverLayers(context)
      decorations.forEach { it.drawOverLayers(context) }
      forEachPersistentMarker { marker, targets -> marker.drawOverLayers(context, targets) }
      legend?.draw(context)
      if (markerTargets.isNotEmpty()) marker?.drawOverLayers(context, markerTargets)
    }
  }

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public fun updateRanges(ranges: MutableCartesianChartRanges, model: CartesianChartModel) {
    ranges.xStep = getXStep(model)
    model.forEachWithLayer(rangeUpdateConsumer.apply { this.ranges = ranges })
  }

  override fun updateLayerMargins(
    context: CartesianMeasuringContext,
    layerMargins: CartesianLayerMargins,
    layerDimensions: CartesianLayerDimensions,
    model: CartesianChartModel,
  ) {
    context.model.forEachWithLayer(
      layerMarginUpdateConsumer.apply {
        this.context = context
        this.layerDimensions = layerDimensions
        this.layerMargins = layerMargins
      }
    )
  }

  override fun updateHorizontalLayerMargins(
    context: CartesianMeasuringContext,
    horizontalLayerMargins: HorizontalCartesianLayerMargins,
    layerHeight: Float,
    model: CartesianChartModel,
  ) {
    context.model.forEachWithLayer(
      horizontalLayerMarginUpdateConsumer.apply {
        this.context = context
        this.horizontalLayerMargins = horizontalLayerMargins
        this.layerHeight = layerHeight
      }
    )
  }

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public fun prepareForTransformation(
    model: CartesianChartModel?,
    extraStore: MutableExtraStore,
    ranges: CartesianChartRanges,
  ) {
    model?.forEachWithLayer(
      transformationPreparationConsumer.apply {
        this.extraStore = extraStore
        this.ranges = ranges
      }
    ) ?: layers.forEach { it.prepareForTransformation(null, ranges, extraStore) }
  }

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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

  private inline fun forEachPersistentMarker(
    block: (CartesianMarker, List<CartesianMarker.Target>) -> Unit
  ) {
    persistentMarkerMap.forEach { (x, marker) ->
      markerTargets[x]?.also { targets -> block(marker, targets) }
    }
  }

  protected open fun getMarkerTargets(
    context: CartesianDrawingContext,
    pointerPosition: Point?,
  ): List<CartesianMarker.Target> {
    val marker = marker ?: return emptyList()
    if (pointerPosition == null || markerTargets.isEmpty()) {
      if (previousMarkerTargetHashCode != null) markerVisibilityListener?.onHidden(marker)
      previousMarkerTargetHashCode = null
      return emptyList()
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
    val targetHashCode = targets.hashCode()
    if (previousMarkerTargetHashCode == null) {
      markerVisibilityListener?.onShown(marker, targets)
    } else if (targetHashCode != previousMarkerTargetHashCode) {
      markerVisibilityListener?.onUpdated(marker, targets)
    }
    previousMarkerTargetHashCode = targetHashCode
    return targets
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

  /** Creates a new [CartesianChart] based on this one. */
  public fun copy(
    vararg layers: CartesianLayer<*> = this.layers.toTypedArray(),
    startAxis: Axis<Axis.Position.Vertical.Start>? = this.startAxis,
    topAxis: Axis<Axis.Position.Horizontal.Top>? = this.topAxis,
    endAxis: Axis<Axis.Position.Vertical.End>? = this.endAxis,
    bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? = this.bottomAxis,
    marker: CartesianMarker? = this.marker,
    markerVisibilityListener: CartesianMarkerVisibilityListener? = this.markerVisibilityListener,
    layerPadding: ((ExtraStore) -> CartesianLayerPadding) = this.layerPadding,
    legend: Legend<CartesianMeasuringContext, CartesianDrawingContext>? = this.legend,
    fadingEdges: FadingEdges? = this.fadingEdges,
    decorations: List<Decoration> = this.decorations,
    persistentMarkers: (PersistentMarkerScope.(ExtraStore) -> Unit)? = this.persistentMarkers,
    getXStep: ((CartesianChartModel) -> Double) = this.getXStep,
  ): CartesianChart =
    CartesianChart(
      layers = layers,
      startAxis = startAxis,
      topAxis = topAxis,
      endAxis = endAxis,
      bottomAxis = bottomAxis,
      marker = marker,
      markerVisibilityListener = markerVisibilityListener,
      layerPadding = layerPadding,
      legend = legend,
      fadingEdges = fadingEdges,
      decorations = decorations,
      persistentMarkers = persistentMarkers,
      getXStep = getXStep,
      id = id,
      previousMarkerTargetHashCode = previousMarkerTargetHashCode,
      persistentMarkerMap = persistentMarkerMap,
      previousPersistentMarkerHashCode = previousPersistentMarkerHashCode,
    )

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is CartesianChart &&
        id == other.id &&
        marker == other.marker &&
        markerVisibilityListener == other.markerVisibilityListener &&
        layerPadding == other.layerPadding &&
        legend == other.legend &&
        fadingEdges == other.fadingEdges &&
        decorations == other.decorations &&
        persistentMarkers == other.persistentMarkers &&
        getXStep == other.getXStep &&
        layers == other.layers &&
        startAxis == other.startAxis &&
        topAxis == other.topAxis &&
        endAxis == other.endAxis &&
        bottomAxis == other.bottomAxis

  override fun hashCode(): Int {
    var result = marker.hashCode()
    result = 31 * result + markerVisibilityListener.hashCode()
    result = 31 * result + layerPadding.hashCode()
    result = 31 * result + legend.hashCode()
    result = 31 * result + fadingEdges.hashCode()
    result = 31 * result + decorations.hashCode()
    result = 31 * result + persistentMarkers.hashCode()
    result = 31 * result + getXStep.hashCode()
    result = 31 * result + layers.hashCode()
    result = 31 * result + startAxis.hashCode()
    result = 31 * result + topAxis.hashCode()
    result = 31 * result + endAxis.hashCode()
    result = 31 * result + bottomAxis.hashCode()
    result = 31 * result + id.hashCode()
    return result
  }

  /** Facilitates adding persistent [CartesianMarker]s to [CartesianChart]s. */
  public fun interface PersistentMarkerScope {
    /** Adds this [CartesianMarker] at [x]. */
    public infix fun CartesianMarker.at(x: Number)
  }

  protected companion object {
    public val cacheKeyNamespace: CacheStore.KeyNamespace = CacheStore.KeyNamespace()
  }
}
