/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

@file:OptIn(ExperimentalUuidApi::class)

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart.PersistentMarkerScope
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.AxisManager
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.compose.common.*
import com.patrykandpatrick.vico.compose.common.data.CacheStore
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.data.MutableExtraStore
import com.patrykandpatrick.vico.compose.common.gcdWith
import com.patrykandpatrick.vico.compose.common.orZero
import kotlin.math.abs
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val AreaFillsPhase = setOf(CartesianLayer.DrawingPhase.AreaFills)

private val ContentPhase = setOf(CartesianLayer.DrawingPhase.Content)

private fun getDefaultXStep(model: CartesianChartModel, minX: Double): Double {
  var gcd = model.getXDeltaGcd()
  if (model.models.isEmpty()) return gcd
  val minXOffset = model.models.minOf { it.minX } - minX
  if (minXOffset != 0.0) {
    gcd = gcd.gcdWith(abs(minXOffset))
    require(gcd != 0.0) {
      "The x-values are too precise. The maximum precision is four decimal places."
    }
  }
  return gcd
}

/** A chart based on a Cartesian coordinate plane, composed of [CartesianLayer]s. */
@Stable
public open class CartesianChart
internal constructor(
  vararg layers: CartesianLayer<*>,
  startAxis: Axis<Axis.Position.Vertical.Start>? = null,
  topAxis: Axis<Axis.Position.Horizontal.Top>? = null,
  endAxis: Axis<Axis.Position.Vertical.End>? = null,
  bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? = null,
  internal val marker: CartesianMarker? = null,
  protected val markerVisibilityListener: CartesianMarkerVisibilityListener? = null,
  internal val layerPadding: ((ExtraStore) -> CartesianLayerPadding) = { CartesianLayerPadding() },
  protected val legend: Legend<CartesianMeasuringContext, CartesianDrawingContext>? = null,
  protected val fadingEdges: FadingEdges? = null,
  protected val decorations: List<Decoration> = emptyList(),
  protected val persistentMarkers: (PersistentMarkerScope.(ExtraStore) -> Unit)? = null,
  protected val getXStep: ((CartesianChartModel, Double, Double) -> Double) = { model, minX, _ ->
    getDefaultXStep(model, minX)
  },
  public val markerController: CartesianMarkerController = CartesianMarkerController.showOnPress(),
  internal val id: Uuid = Uuid.random(),
  private var previousMarkerTargetHashCode: Int? = null,
  private val persistentMarkerMap: MutableMap<Double, CartesianMarker> = mutableMapOf(),
  private var previousPersistentMarkerHashCode: Int? = null,
) : CartesianLayerMarginUpdater<CartesianChartModel> {
  private val persistentMarkerScope = PersistentMarkerScope {
    persistentMarkerMap[it.toDouble()] = this
  }

  private val layerMargins = CartesianLayerMargins()
  private val layerDrawScope = CanvasDrawScope()
  private val axisManager = AxisManager()
  private val _markerTargets = mutableMapOf<Double, MutableList<CartesianMarker.Target>>()

  /**
   * Whether each [CartesianLayer] separates its area fills this frame, in [layers] order, and the
   * index of the first one that does, or −1. Recorded once per frame by [separabilityConsumer] so
   * that the drawing traversals don’t have to ask again.
   */
  private val layerSeparability = mutableListOf<Boolean>()

  private var firstSeparableLayerIndex = -1

  private val drawingConsumer =
    object : ModelAndLayerConsumer {
      lateinit var context: CartesianDrawingContext

      /**
       * The [CartesianLayer.DrawingPhase] this traversal draws, or `null` to draw every phase in
       * one pass.
       */
      private var phase: CartesianLayer.DrawingPhase? = null

      private var index = 0

      fun startTraversal(phase: CartesianLayer.DrawingPhase?) {
        this.phase = phase
        index = 0
      }

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        val index = index++
        val layerModel = model ?: return
        val phases = phases(index) ?: return
        layer.draw(context, layerModel, phases)
        if (CartesianLayer.DrawingPhase.Content in phases) {
          layer.markerTargets.forEach {
            _markerTargets.getOrPut(it.key) { mutableListOf() } += it.value
          }
        }
      }

      /**
       * The phases the [CartesianLayer] at [index] draws during this traversal, or `null` if it
       * draws nothing. A [CartesianLayer] that can’t separate its area fills is drawn in full,
       * during whichever traversal keeps its position relative to the other [CartesianLayer]s—so
       * the only content that the interruption moves is a separating [CartesianLayer]’s own area
       * fills.
       */
      private fun phases(index: Int): Set<CartesianLayer.DrawingPhase>? {
        val phase = phase ?: return CartesianLayer.DrawingPhase.All
        if (layerSeparability.getOrElse(index) { false }) {
          return when (phase) {
            CartesianLayer.DrawingPhase.AreaFills -> AreaFillsPhase
            CartesianLayer.DrawingPhase.Content -> ContentPhase
          }
        }
        val fullDrawPhase =
          if (index > firstSeparableLayerIndex) {
            CartesianLayer.DrawingPhase.Content
          } else {
            CartesianLayer.DrawingPhase.AreaFills
          }
        return if (phase == fullDrawPhase) CartesianLayer.DrawingPhase.All else null
      }
    }

  private val separabilityConsumer =
    object : ModelAndLayerConsumer {
      lateinit var context: CartesianDrawingContext

      override fun <T : CartesianLayerModel> invoke(model: T?, layer: CartesianLayer<T>) {
        val separatesAreaFills = model != null && layer.canSeparateAreaFills(context, model)
        if (separatesAreaFills && firstSeparableLayerIndex < 0) {
          firstSeparableLayerIndex = layerSeparability.size
        }
        layerSeparability += separatesAreaFills
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

  /** Links _x_-values to [CartesianMarker.Target]s. */
  protected val markerTargets: Map<Double, List<CartesianMarker.Target>> = _markerTargets

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

  protected constructor(
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
    getXStep: ((CartesianChartModel, Double, Double) -> Double) = { model, minX, _ ->
      getDefaultXStep(model, minX)
    },
    markerController: CartesianMarkerController = CartesianMarkerController.showOnPress(),
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
    markerController = markerController,
    id = Uuid.random(),
    previousMarkerTargetHashCode = null,
    persistentMarkerMap = mutableMapOf(),
    previousPersistentMarkerHashCode = null,
  )

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
   * @param getXStep defines the _x_-step (the difference between neighboring major _x_-values).
   * @param markerController controls [marker] visibility.
   */
  @Deprecated(
    message =
      "Use the constructor whose `getXStep` lambda also receives the final minimum and maximum " +
        "x-values."
  )
  protected constructor(
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
    getXStep: ((CartesianChartModel) -> Double),
    markerController: CartesianMarkerController = CartesianMarkerController.showOnPress(),
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
    getXStep = { model, _, _ -> getXStep(model) },
    markerController = markerController,
    id = Uuid.random(),
    previousMarkerTargetHashCode = null,
    persistentMarkerMap = mutableMapOf(),
    previousPersistentMarkerHashCode = null,
  )

  private fun setLayerBounds(left: Float, top: Float, right: Float, bottom: Float) {
    layerBounds = Rect(left, top, right, bottom)
  }

  private fun marginUpdaters(): List<CartesianLayerMarginUpdater<CartesianChartModel>> = buildList {
    add(this@CartesianChart)
    addAll(axisManager.axisCache)
    marker?.let(::add)
    addAll(persistentMarkerMap.values)
  }

  /**
   * Populates [layerMargins] (and [layerDimensions]) and returns the legend height. The result
   * depends on [CartesianMeasuringContext.canvasWidth] but not the canvas height, so it can be used
   * to measure the chart before its height is known.
   */
  private fun measure(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
  ): Float =
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
      marginUpdaters().forEach { updater ->
        updater.updateLayerMargins(context, layerMargins, layerDimensions, model)
      }
      legend?.getHeight(context, canvasWidth).orZero
    }

  /**
   * Returns the total vertical space taken up by the layer margins and the legend—i.e., the height
   * the chart needs in addition to the coordinate system. This is height-independent, so it can be
   * used to derive the chart’s height from a desired coordinate-system height.
   */
  internal fun getVerticalExtras(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
  ): Float {
    val legendHeight = measure(context, layerDimensions)
    return layerMargins.vertical + legendHeight
  }

  internal fun prepare(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
    canvasSize: Size,
  ) {
    with(context) {
      val legendHeight = measure(context, layerDimensions)
      val freeHeight = canvasSize.height - layerMargins.vertical - legendHeight
      marginUpdaters().forEach { updater ->
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

  /**
   * Whether layer drawing should be interrupted so that content can be drawn over the
   * [CartesianLayer]s’ area fills. Requires both a participant that draws there and a
   * [CartesianLayer] that can separate its area fills—otherwise there’s nothing to interrupt, and
   * the participants draw at the [DrawingOrder.UnderLayers] position instead. Records
   * [layerSeparability] and [firstSeparableLayerIndex] for the drawing traversals.
   */
  private fun hasContentOverAreaFills(context: CartesianDrawingContext): Boolean {
    if (!axisManager.hasContentOverAreaFills() && decorations.none { it.hasContentOverAreaFills }) {
      return false
    }
    layerSeparability.clear()
    firstSeparableLayerIndex = -1
    separabilityConsumer.context = context
    context.model.forEachWithLayer(separabilityConsumer)
    return firstSeparableLayerIndex >= 0
  }

  private fun drawOverAreaFills(context: CartesianDrawingContext) {
    axisManager.drawOverAreaFills(context)
    decorations.forEach { if (it.hasContentOverAreaFills) it.drawOverAreaFills(context) }
  }

  private fun updatePersistentMarkers(extraStore: ExtraStore) {
    persistentMarkerMap.clear()
    persistentMarkers?.invoke(persistentMarkerScope, extraStore)
  }

  internal fun draw(context: CartesianDrawingContext) {
    with(context) {
      if (fadingEdges != null) canvas.saveLayer(Rect(Offset.Zero, canvasSize), EmptyPaint)
      decorations.forEach { it.drawUnderLayers(context) }
      axisManager.drawUnderLayers(context)
      val (layerBitmap, layerCanvas) = getBitmap(cacheKeyNamespace)
      // The layer canvas needs a matching `DrawScope`: components that draw via
      // `mutableDrawScope` (shadows) would otherwise paint onto the main canvas, which is
      // composited under `layerBitmap`, detaching them from the components they belong to.
      layerDrawScope.draw(density, layoutDirection, layerCanvas, canvasSize) {
        withCanvas(layerCanvas, MutableDrawScope(this)) {
          drawingConsumer.context = context
          if (hasContentOverAreaFills(context)) {
            model.forEachWithLayer(
              drawingConsumer.apply { startTraversal(CartesianLayer.DrawingPhase.AreaFills) }
            )
            drawOverAreaFills(context)
            model.forEachWithLayer(
              drawingConsumer.apply { startTraversal(CartesianLayer.DrawingPhase.Content) }
            )
          } else {
            // Nothing to interrupt, so the `OverAreaFills` participants fall back to the
            // `UnderLayers` position rather than not being drawn at all.
            drawOverAreaFills(context)
            model.forEachWithLayer(drawingConsumer.apply { startTraversal(null) })
          }
        }
      }
      val sortedMarkerTargetPairs = _markerTargets.toList().sortedBy { it.first }
      _markerTargets.clear()
      _markerTargets.putAll(sortedMarkerTargetPairs)
      forEachPersistentMarker { marker, targets -> marker.drawUnderLayers(context, targets) }
      val markerTargets = getMarkerTargets(markerX, markerSeriesIndex)
      val drawMarker = markerTargets.isNotEmpty()
      if (drawMarker) marker?.drawUnderLayers(context, markerTargets)
      canvas.drawImage(layerBitmap, Offset.Zero, EmptyPaint)
      fadingEdges?.run {
        draw(context)
        canvas.restore()
      }
      // Drawn outside the fading-edges group, for the axes and the decorations alike: the fade
      // applies to the layers’ content, not to what’s drawn over them. `VerticalAxis` draws its
      // labels here, and `HorizontalLabelPosition.Inside` puts them within `layerBounds`, where
      // the fade would erase them.
      axisManager.drawOverLayers(context)
      decorations.forEach { it.drawOverLayers(context) }
      forEachPersistentMarker { marker, targets -> marker.drawOverLayers(context, targets) }
      legend?.draw(context)
      if (drawMarker) marker?.drawOverLayers(context, markerTargets)
    }
  }

  internal fun updateRanges(ranges: MutableCartesianChartRanges, model: CartesianChartModel) {
    model.forEachWithLayer(rangeUpdateConsumer.apply { this.ranges = ranges })
    ranges.xStep = getXStep(model, ranges.minX, ranges.maxX)
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

  /** Returns the `CartesianMarker.Target`s for `x`. */
  public open fun getMarkerTargets(
    x: Double?,
    visibleXRange: ClosedFloatingPointRange<Double>,
  ): List<CartesianMarker.Target> =
    if (x == null || x !in visibleXRange || markerTargets.isEmpty()) {
      emptyList()
    } else {
      var targets = emptyList<CartesianMarker.Target>()
      var previousDelta = Double.POSITIVE_INFINITY
      for ((key, keyTargets) in markerTargets) {
        val delta = abs(key - x)
        if (delta > previousDelta) break
        targets = keyTargets
        previousDelta = delta
      }
      targets
    }

  private fun getMarkerTargets(x: Double?, seriesIndex: Int?): List<CartesianMarker.Target> {
    val marker = marker ?: return emptyList()
    return if (x == null || markerTargets.isEmpty()) {
      if (previousMarkerTargetHashCode != null) markerVisibilityListener?.onHidden(marker)
      previousMarkerTargetHashCode = null
      emptyList()
    } else {
      val allTargets = markerTargets[x] ?: return emptyList()
      val targets =
        if (seriesIndex != null) {
          val target =
            allTargets.getOrNull(seriesIndex)
              ?: return run {
                if (previousMarkerTargetHashCode != null) markerVisibilityListener?.onHidden(marker)
                previousMarkerTargetHashCode = null
                emptyList()
              }
          listOf(target)
        } else {
          allTargets
        }
      val targetHashCode = targets.hashCode()
      if (previousMarkerTargetHashCode == null) {
        markerVisibilityListener?.onShown(marker, targets)
      } else if (targetHashCode != previousMarkerTargetHashCode) {
        markerVisibilityListener?.onUpdated(marker, targets)
      }
      previousMarkerTargetHashCode = targetHashCode
      targets
    }
  }

  protected inline fun <reified T : CartesianLayerModel> MutableList<CartesianLayerModel>.consume(
    layer: CartesianLayer<T>,
    consumer: ModelAndLayerConsumer,
  ) {
    @Suppress("UNCHECKED_CAST") val model = firstOrNull { it is T } as T?
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
    getXStep: ((CartesianChartModel, Double, Double) -> Double) = this.getXStep,
    markerController: CartesianMarkerController = CartesianMarkerController.showOnPress(),
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
      markerController = markerController,
      id = id,
      previousMarkerTargetHashCode = previousMarkerTargetHashCode,
      persistentMarkerMap = persistentMarkerMap,
      previousPersistentMarkerHashCode = previousPersistentMarkerHashCode,
    )

  /** Creates a new [CartesianChart] based on this one. */
  @Deprecated(
    message =
      "Use the overload whose `getXStep` lambda also receives the final minimum and maximum " +
        "x-values."
  )
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
    getXStep: ((CartesianChartModel) -> Double),
    markerController: CartesianMarkerController = CartesianMarkerController.showOnPress(),
  ): CartesianChart =
    copy(
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
      getXStep = { model, _, _ -> getXStep(model) },
      markerController = markerController,
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
        bottomAxis == other.bottomAxis &&
        markerController == other.markerController

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
    result = 31 * result + markerController.hashCode()
    return result
  }

  /**
   * Defines where content is drawn relative to the [CartesianLayer]s. Used by [Axis]es and
   * [Decoration]s.
   *
   * [OverAreaFills] requires interrupting layer drawing, which is subject to the following rules.
   * 1. An interruption never splits a [CartesianLayer]’s opacity group, so it’s skipped while a
   *    [CartesianLayer] is fading in—as one does when its model first appears. (Inserting content
   *    into an opacity group changes how the group’s own contents composite, and the opacity is the
   *    [CartesianLayer]’s, so it can’t be hoisted.) It’s likewise skipped when no [CartesianLayer]
   *    can separate its area fills, since then there’s nothing to interrupt. In both cases the
   *    content is drawn under the [CartesianLayer]s instead, though into the same offscreen
   *    [Canvas] as their content rather than onto the [CartesianChart]’s own, so it’s over anything
   *    a [CartesianMarker] draws under the layers.
   * 2. Content drawn at an interruption goes to the same [Canvas] and `DrawScope` as the
   *    [CartesianLayer]s’ content, so it composites with them rather than beneath them.
   * 3. Content drawn at an interruption isn’t clipped. A participant that draws beyond
   *    [CartesianDrawingContext.layerBounds] does so—the axis line and the outward halves of the
   *    ticks, for instance—and one that shouldn’t clips itself.
   * 4. [CartesianLayer] order is preserved: the only content that moves is a participating
   *    [CartesianLayer]’s own area fills, which are drawn under the content at the interruption. A
   *    [CartesianLayer] that has no area fills, or that can’t draw them separately, is drawn in
   *    full, on whichever side of the interruption keeps its position relative to the other
   *    [CartesianLayer]s.
   */
  public enum class DrawingOrder {
    /** Draws the content under the [CartesianLayer]s. */
    UnderLayers,
    /**
     * Draws the content over the [CartesianLayer]s’ area fills and under the rest of their
     * content—strokes, points, and data labels.
     */
    OverAreaFills,
    /** Draws the content over the [CartesianLayer]s. */
    OverLayers,
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
 * @param layers the [CartesianLayer]s.
 * @param startAxis the start [Axis].
 * @param topAxis the top [Axis].
 * @param endAxis the end [Axis].
 * @param bottomAxis the bottom [Axis].
 * @param marker appears when the [CartesianChart] is tapped.
 * @param markerVisibilityListener allows for listening to [marker] visibility changes.
 * @param layerPadding returns the [CartesianLayerPadding].
 * @param legend the legend.
 * @param fadingEdges applies a horizontal fade to the edges of the [CartesianChart], provided that
 *   it’s scrollable.
 * @param decorations the [Decoration]s.
 * @param persistentMarkers adds persistent [CartesianMarker]s.
 * @param getXStep defines the _x_-step (the difference between neighboring major _x_-values).
 *   Receives the model and the final minimum and maximum _x_-values.
 * @param markerController controls [marker] visibility.
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
  getXStep: ((CartesianChartModel, Double, Double) -> Double) = { model, minX, _ ->
    getDefaultXStep(model, minX)
  },
  markerController: CartesianMarkerController = CartesianMarkerController.rememberShowOnPress(),
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
    markerController,
  ) {
    val cartesianChart =
      wrapper.value?.copy(
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
        markerController = markerController,
      )
        ?: CartesianChart(
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
          markerController = markerController,
        )
    wrapper.value = cartesianChart
    cartesianChart
  }
}

/**
 * Creates and remembers a [CartesianChart].
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
 * @param fadingEdges applies a horizontal fade to the edges of the [CartesianChart], provided that
 *   it’s scrollable.
 * @param decorations the [Decoration]s.
 * @param persistentMarkers adds persistent [CartesianMarker]s.
 * @param getXStep defines the _x_-step (the difference between neighboring major _x_-values).
 * @param markerController controls [marker] visibility.
 * @see rememberCandlestickCartesianLayer
 * @see rememberColumnCartesianLayer
 * @see rememberLineCartesianLayer
 */
@Deprecated(
  message =
    "Use the overload whose `getXStep` lambda also receives the final minimum and maximum x-values."
)
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
  getXStep: ((CartesianChartModel) -> Double),
  markerController: CartesianMarkerController = CartesianMarkerController.rememberShowOnPress(),
): CartesianChart =
  rememberCartesianChart(
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
    getXStep = { model, _, _ -> getXStep(model) },
    markerController = markerController,
  )
