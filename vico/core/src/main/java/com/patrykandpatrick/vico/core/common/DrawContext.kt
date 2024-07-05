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

package com.patrykandpatrick.vico.core.common

import android.graphics.Canvas
import android.graphics.RectF
import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.data.CacheStore
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore

/**
 * [DrawContext] is an extension of [MeasureContext] that stores a [Canvas] and other properties. It
 * also defines helpful drawing functions.
 */
public interface DrawContext : MeasureContext {
  /** The elevation overlay color, applied to [ShapeComponent]s that cast shadows. */
  public val elevationOverlayColor: Long

  /** The canvas to draw the chart on. */
  public val canvas: Canvas

  /**
   * Saves the [Canvas] state.
   *
   * @see Canvas.save
   */
  public fun saveCanvas(): Int = canvas.save()

  /** Temporarily swaps the [Canvas] and yields [DrawContext] as the [block]’s receiver. */
  public fun withOtherCanvas(canvas: Canvas, block: (DrawContext) -> Unit)

  /**
   * Clips the [Canvas] to the specified rectangle.
   *
   * @see Canvas.clipRect
   */
  public fun clipRect(left: Float, top: Float, right: Float, bottom: Float) {
    canvas.clipRect(left, top, right, bottom)
  }

  /**
   * Clips the [Canvas] to the specified [rectF].
   *
   * @see Canvas.clipRect
   */
  public fun clipRect(rectF: RectF) {
    canvas.clipRect(rectF)
  }

  /**
   * Restores the [Canvas] state.
   *
   * @see Canvas.restore
   */
  public fun restoreCanvas() {
    canvas.restore()
  }

  /**
   * Restores the [Canvas] state to the given save level.
   *
   * @see Canvas.restoreToCount
   */
  public fun restoreCanvasToCount(count: Int) {
    canvas.restoreToCount(count)
  }

  /**
   * A convenience function for [Canvas.saveLayer].
   *
   * @see Canvas.saveLayer
   */
  public fun saveLayer(
    left: Float = 0f,
    top: Float = 0f,
    right: Float = canvas.width.toFloat(),
    bottom: Float = canvas.height.toFloat(),
  ): Int = canvas.saveLayer(left, top, right, bottom)
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun drawContext(
  canvas: Canvas,
  density: Float = 1f,
  isLtr: Boolean = true,
  elevationOverlayColor: Long = DefaultColors.Light.elevationOverlayColor,
  canvasBounds: RectF = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat()),
  spToPx: (Float) -> Float = { it },
): DrawContext =
  object : DrawContext {
    override val canvasBounds: RectF = canvasBounds

    override val elevationOverlayColor: Long = elevationOverlayColor

    override var canvas: Canvas = canvas

    override val density: Float = density

    override val isLtr: Boolean = isLtr

    @Deprecated(
      "To cache drawing data, use `cacheStore`. If using `extraStore` for communication between " +
        "functions or classes, switch to a suitable alternative."
    )
    override val extraStore: MutableExtraStore = MutableExtraStore()

    override val cacheStore: CacheStore = CacheStore()

    override fun withOtherCanvas(canvas: Canvas, block: (DrawContext) -> Unit) {
      val originalCanvas = this.canvas
      this.canvas = canvas
      block(this)
      this.canvas = originalCanvas
    }

    override fun spToPx(sp: Float): Float = spToPx(sp)
  }
