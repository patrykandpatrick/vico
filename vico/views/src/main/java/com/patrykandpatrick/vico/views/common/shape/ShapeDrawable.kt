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

package com.patrykandpatrick.vico.views.common.shape

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LayoutDirection
import com.patrykandpatrick.vico.core.common.drawContext
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.views.common.density
import com.patrykandpatrick.vico.views.common.isLtr

/**
 * Creates a [Drawable] out of the provided [shape].
 *
 * @param shape the [Shape] used as a [Drawable].
 * @param isLtr whether the device layout is left-to-right.
 * @param density the pixel density of the device screen.
 * @param width the width of the [Drawable].
 * @param height the height of the [Drawable].
 */
public class ShapeDrawable(
  private val shape: Shape,
  private val isLtr: Boolean,
  private val density: Float,
  private val width: Int = 0,
  private val height: Int = 0,
) : Drawable() {
  public constructor(
    context: Context,
    shape: Shape,
    width: Int = 0,
    height: Int = 0,
  ) : this(
    shape = shape,
    density = context.density,
    isLtr = context.isLtr,
    width = width,
    height = height,
  )

  private val path: Path = Path()

  private var tintList: ColorStateList? = null

  /** The [Paint] used to draw the shape. */
  public val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = DEF_COLOR }

  init {
    setBounds(0, 0, width, height)
  }

  override fun draw(canvas: Canvas) {
    shape.draw(
      drawContext(canvas = canvas, density = density, isLtr = isLtr()),
      paint = paint,
      path = path,
      left = bounds.left.toFloat(),
      top = bounds.top.toFloat(),
      right = bounds.right.toFloat(),
      bottom = bounds.bottom.toFloat(),
    )
    path.reset()
  }

  private fun isLtr(): Boolean =
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
      layoutDirection == LayoutDirection.LTR
    } else {
      isLtr
    }

  override fun setAlpha(alpha: Int) {
    paint.alpha = alpha
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    paint.colorFilter = colorFilter
  }

  override fun setTintList(tint: ColorStateList?) {
    tintList = tint
    updateColor()
  }

  override fun setState(stateSet: IntArray): Boolean {
    val result = super.setState(stateSet)
    updateColor()
    return result
  }

  private fun updateColor() {
    paint.color = tintList?.getColorForState(state, DEF_COLOR) ?: DEF_COLOR
  }

  @Deprecated("`Drawable#getOpacity` is deprecated.")
  override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

  override fun getIntrinsicWidth(): Int = width

  override fun getIntrinsicHeight(): Int = height

  private companion object {
    const val DEF_COLOR = Color.BLACK
  }
}

/**
 * Converts this [Shape] to a [Drawable].
 *
 * @param intrinsicWidth the width of the [Drawable].
 * @param intrinsicHeight the height of the [Drawable].
 */
public fun Shape.toDrawable(
  context: Context,
  intrinsicWidth: Int = 0,
  intrinsicHeight: Int = 0,
): Drawable = ShapeDrawable(context, this, intrinsicWidth, intrinsicHeight)
