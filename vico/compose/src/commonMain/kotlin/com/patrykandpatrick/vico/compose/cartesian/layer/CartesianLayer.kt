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

package com.patrykandpatrick.vico.compose.cartesian.layer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker

/**
 * Visualizes data on a Cartesian plane. [CartesianLayer]s carry their own data ([model]) and are
 * combined and drawn by [CartesianChart]s.
 *
 * A [CartesianLayer] has two kinds of state:
 * - _Configuration_: [model] and the settings passed on creation. This is immutable—when the data
 *   or the settings change, a new [CartesianLayer] is created—and it should determine `equals`.
 * - _Draw-time scratch state_: [markerTargets], which [draw] populates, and any other caches used
 *   while measuring and drawing. This is owned by the main thread: only [draw], [updateDimensions],
 *   and the [CartesianLayerMarginUpdater] functions, which run on the main thread, may touch it.
 *
 * [updateChartRanges] and [AnimatedCartesianLayer.createDrawingModel] may run off the main thread,
 * concurrently with [draw] (of this instance, if it’s kept across a data update of another layer).
 * They may read only the configuration and caches that are initialized thread-safely (e.g., via
 * `lazy`) and immutable once initialized.
 *
 * To animate data changes, implement [AnimatedCartesianLayer].
 *
 * @see BaseCartesianLayer
 */
public interface CartesianLayer<M : CartesianLayerModel> : CartesianLayerMarginUpdater {
  /** The [CartesianLayer]’s data. */
  public val model: M

  /**
   * Links _x_-values to [CartesianMarker.Target]s. This is draw-time scratch state: [draw]
   * populates it on the main thread, and the [CartesianChart] reads it right after.
   */
  public val markerTargets: Map<Double, List<CartesianMarker.Target>>

  /** Draws the [CartesianLayer]. */
  public fun draw(context: CartesianDrawingContext)

  /** Updates [dimensions] to match this [CartesianLayer]’s dimensions. */
  public fun updateDimensions(
    context: CartesianMeasuringContext,
    dimensions: MutableCartesianLayerDimensions,
  )

  /**
   * Updates [chartRanges] in accordance with [model]. This is called before the [CartesianLayer] is
   * first measured or drawn—off the main thread for asynchronous updates, and concurrently with the
   * drawing of other [CartesianLayer]s—so it must only read this [CartesianLayer]’s immutable
   * properties. It’s a suitable place to initialize per-model caches thread-safely (e.g., via
   * `lazy`), so that they aren’t computed on the main thread.
   */
  public fun updateChartRanges(chartRanges: MutableCartesianChartRanges)
}

/**
 * A [CartesianLayer] whose data changes are animated. When a [CartesianChartHost] receives a new
 * [AnimatedCartesianLayer], it pairs it with its predecessor by [key], creates a drawing model for
 * the new data via [createDrawingModel], and animates from the drawing model on screen to the new
 * one via [drawingModelInterpolator]. If there’s no predecessor, the [AnimatedCartesianLayer]
 * animates in. The host owns all animation state; while drawing, retrieve the current drawing model
 * via [CartesianDrawingContext.getDrawingModel].
 *
 * @param D the drawing-model type. Drawing models should be immutable and implement `equals`
 *   structurally.
 */
public interface AnimatedCartesianLayer<M : CartesianLayerModel, D : Any> : CartesianLayer<M> {
  /**
   * Identifies this [AnimatedCartesianLayer] across data updates. The successor of an
   * [AnimatedCartesianLayer] (the one created when its data or settings change) must have an equal
   * [key], and [key]s must be unique among a [CartesianChart]’s [CartesianLayer]s. In a composable
   * function, use [rememberCartesianLayerKey].
   */
  public val key: Any

  /** Creates transitions between drawing models. */
  public val drawingModelInterpolator: CartesianLayerDrawingModelInterpolator<D>

  /**
   * Creates the drawing model for [model] and [ranges]. This is called off the main thread,
   * concurrently with [draw], so it must be a pure function of [ranges] and this
   * [AnimatedCartesianLayer]’s immutable properties.
   */
  public fun createDrawingModel(ranges: CartesianChartRanges): D
}

/**
 * Creates and remembers a key for an [AnimatedCartesianLayer], tied to the current composition
 * slot. Pass it to the [AnimatedCartesianLayer] that’s created at this slot, so that its successors
 * are paired with it.
 */
@Composable public fun rememberCartesianLayerKey(): Any = remember { CartesianLayerKey() }

private class CartesianLayerKey
