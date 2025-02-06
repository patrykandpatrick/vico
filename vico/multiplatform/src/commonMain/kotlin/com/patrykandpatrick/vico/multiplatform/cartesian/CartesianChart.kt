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

package com.patrykandpatrick.vico.multiplatform.cartesian

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChart.PersistentMarkerScope
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.Axis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.AxisManager
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.multiplatform.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerMarginUpdater
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.HorizontalCartesianLayerMargins
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberCandlestickCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.multiplatform.common.EmptyPaint
import com.patrykandpatrick.vico.multiplatform.common.Legend
import com.patrykandpatrick.vico.multiplatform.common.Point
import com.patrykandpatrick.vico.multiplatform.common.ValueWrapper
import com.patrykandpatrick.vico.multiplatform.common.data.CacheStore
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import com.patrykandpatrick.vico.multiplatform.common.data.MutableExtraStore
import com.patrykandpatrick.vico.multiplatform.common.getBitmap
import com.patrykandpatrick.vico.multiplatform.common.orZero
import kotlin.math.abs
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
 * @property layerPadding returns the [CartesianLayerPadding].
 * @property legend the legend.
 * @property fadingEdges applies a horizontal fade to the edges of the [CartesianChart], provided
 *   that it’s scrollable.
 * @property decorations the [Decoration]s.
 * @property persistentMarkers adds persistent [CartesianMarker]s.
 * @property getXStep defines the _x_ step (the difference between neighboring major _x_ values).
 */
@Stable
public open class CartesianChart(
  vararg layers: CartesianLayer<*>,
  startAxis: Axis<Axis.Position.Vertical.Start>? = null,
  topAxis: Axis<Axis.Position.Horizontal.Top>? = null,
  endAxis: Axis<Axis.Position.Vertical.End>? = null,
  bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? = null,
  @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) public val marker: CartesianMarker? = null,
  protected val markerVisibilityListener: CartesianMarkerVisibilityListener? = null,
  @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public val layerPadding: ((ExtraStore) -> CartesianLayerPadding) = { CartesianLayerPadding() },
  protected val legend: Legend<CartesianMeasuringContext, CartesianDrawingContext>? = null,
  protected val fadingEdges: FadingEdges? = null,
  protected val decorations: List<Decoration> = emptyList(),
  protected val persistentMarkers: (PersistentMarkerScope.(ExtraStore) -> Unit)? = null,
  protected val getXStep: ((CartesianChartModel) -> Double) = { it.getXDeltaGcd() },
) : CartesianLayerMarginUpdater<CartesianChartModel> {
  private val persistentMarkerMap = mutableMapOf<Double, CartesianMarker>()
  private val persistentMarkerScope = PersistentMarkerScope {
    persistentMarkerMap[it.toDouble()] = this
  }
  private var previousPersistentMarkerHashCode: Int? = null
  private val layerMargins = CartesianLayerMargins()
  private val axisManager = AxisManager()
  private val _markerTargets = mutableMapOf<Double, MutableList<CartesianMarker.Target>>()
  private var previousMarkerTargetHashCode: Int? = null

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

  internal var layerBounds: Rect = Rect.Zero

  /** The [CartesianLayer]s of which this [CartesianChart] is composed. */
  public val layers: List<CartesianLayer<*>> = layers.toList()

  /** Links _x_ values to [CartesianMarker.Target]s. */
  protected val markerTargets: Map<Double, List<CartesianMarker.Target>> = _markerTargets

  /** The start [Axis]. */
  public val startAxis: Axis<Axis.Position.Vertical.Start>? by axisManager::startAxis

  /** The top [Axis]. */
  public val topAxis: Axis<Axis.Position.Horizontal.Top>? by axisManager::topAxis

  /** The end [Axis]. */
  public val endAxis: Axis<Axis.Position.Vertical.End>? by axisManager::endAxis

  /** The bottom [Axis]. */
  public val bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? by axisManager::bottomAxis

  @OptIn(ExperimentalUuidApi::class)
  internal var id: Uuid = Uuid.random()
    private set

  init {
    axisManager.startAxis = startAxis
    axisManager.topAxis = topAxis
    axisManager.endAxis = endAxis
    axisManager.bottomAxis = bottomAxis
  }

  private fun setLayerBounds(left: Float, top: Float, right: Float, bottom: Float) {
    layerBounds = Rect(left, top, right, bottom)
  }

  internal fun prepare(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
  ) {
    with(context) {
      _markerTargets.clear()
      layerMargins.clear()
      val persistentMarkerHashCode = 31 * persistentMarkers.hashCode() + model.extraStore.hashCode()
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
      val legendHeight = legend?.getHeight(context, canvasSize.width).orZero
      val freeHeight = canvasSize.height - layerMargins.vertical - legendHeight
      marginUpdaters.forEach { updater ->
        updater.updateHorizontalLayerMargins(context, layerMargins, freeHeight, model)
      }
      setLayerBounds(
        layerMargins.getLeft(isLtr),
        layerMargins.top,
        canvasSize.width - layerMargins.getRight(isLtr),
        canvasSize.height - layerMargins.bottom - legendHeight,
      )
      axisManager.setAxesBounds(context, canvasSize, layerBounds, layerMargins)
      legend?.setBounds(
        left = 0,
        top = layerBounds.bottom + layerMargins.bottom,
        right = canvasSize.width,
        bottom = layerBounds.bottom + layerMargins.bottom + legendHeight,
      )
    }
  }

  private fun updatePersistentMarkers(extraStore: ExtraStore) {
    persistentMarkerMap.clear()
    persistentMarkers?.invoke(persistentMarkerScope, extraStore)
  }

  internal fun draw(context: CartesianDrawingContext) {
    with(context) {
      if (fadingEdges != null) canvas.saveLayer(Rect(Offset.Zero, canvasSize), EmptyPaint)
      axisManager.drawUnderLayers(context)
      decorations.forEach { it.drawUnderLayers(context) }
      val (layerBitmap, layerCanvas) = getBitmap(cacheKeyNamespace)
      withCanvas(layerCanvas) {
        model.forEachWithLayer(drawingConsumer.apply { this.context = context })
      }
      val sortedMarkerTargetPairs = _markerTargets.toList().sortedBy { it.first }
      _markerTargets.clear()
      _markerTargets.putAll(sortedMarkerTargetPairs)
      forEachPersistentMarker { marker, targets -> marker.drawUnderLayers(context, targets) }
      val markerTargets = getMarkerTargets(context, pointerPosition)
      if (markerTargets.isNotEmpty()) marker?.drawUnderLayers(context, markerTargets)
      canvas.drawImage(layerBitmap, Offset.Zero, EmptyPaint)
      fadingEdges?.run {
        draw(context)
        canvas.restore()
      }
      axisManager.drawOverLayers(context)
      decorations.forEach { it.drawOverLayers(context) }
      forEachPersistentMarker { marker, targets -> marker.drawOverLayers(context, targets) }
      legend?.draw(context)
      if (markerTargets.isNotEmpty()) marker?.drawOverLayers(context, markerTargets)
    }
  }

  internal fun updateRanges(ranges: MutableCartesianChartRanges, model: CartesianChartModel) {
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

  internal fun prepareForTransformation(
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

  internal suspend fun transform(extraStore: MutableExtraStore, fraction: Float) {
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
        *layers,
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
      )
      .also {
        @OptIn(ExperimentalUuidApi::class)
        it.id = id
      }

  @OptIn(ExperimentalUuidApi::class)
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
        layers == other.layers

  override fun hashCode(): Int {
    var result = marker?.hashCode() ?: 0
    result = 31 * result + (markerVisibilityListener?.hashCode() ?: 0)
    result = 31 * result + layerPadding.hashCode()
    result = 31 * result + (legend?.hashCode() ?: 0)
    result = 31 * result + (fadingEdges?.hashCode() ?: 0)
    result = 31 * result + decorations.hashCode()
    result = 31 * result + (persistentMarkers?.hashCode() ?: 0)
    result = 31 * result + getXStep.hashCode()
    result = 31 * result + layers.hashCode()
    @OptIn(ExperimentalUuidApi::class)
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

/**
 * Creates and remembers a [CartesianChart].
 *
 * @see rememberCandlestickCartesianLayer
 * @see rememberColumnCartesianLayer
 * @see rememberLineCartesianLayer
 */
@Composable
public fun rememberCartesianChart(
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
): CartesianChart {
  val wrapper = remember { ValueWrapper<CartesianChart?>(null) }
  return remember(
    *layers,
    startAxis,
    topAxis,
    endAxis,
    bottomAxis,
    marker,
    markerVisibilityListener,
    layerPadding,
    legend,
    fadingEdges,
    decorations,
    persistentMarkers,
    getXStep,
  ) {
    val cartesianChart =
      wrapper.value?.copy(
        *layers,
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
      )
        ?: CartesianChart(
          *layers,
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
        )
    wrapper.value = cartesianChart
    cartesianChart
  }
}
