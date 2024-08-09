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
import com.patrykandpatrick.vico.core.common.DrawingContext
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.views.common.density
import com.patrykandpatrick.vico.views.common.isLtr

private const val DEFAULT_COLOR = Color.BLACK

/** Converts this [Shape] to a [Drawable]. */
public fun Shape.toDrawable(
  context: Context,
  intrinsicWidth: Int = -1,
  intrinsicHeight: Int = -1,
): Drawable {
  val density = context.density
  val isLtr = context.isLtr
  return object : Drawable() {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = DEFAULT_COLOR }
    val path = Path()

    var tintList: ColorStateList? = null
      @JvmName("setShapeDrawableTintList")
      set(value) {
        field = value
        applyTint()
      }

    init {
      setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    }

    fun applyTint() {
      paint.color = tintList?.getColorForState(state, DEFAULT_COLOR) ?: DEFAULT_COLOR
    }

    override fun draw(canvas: Canvas) {
      outline(
        DrawingContext(
          canvas = canvas,
          density = density,
          isLtr =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              layoutDirection == LayoutDirection.LTR
            } else {
              isLtr
            },
        ),
        path,
        bounds.left.toFloat(),
        bounds.top.toFloat(),
        bounds.right.toFloat(),
        bounds.bottom.toFloat(),
      )
      path.rewind()
    }

    override fun setAlpha(alpha: Int) {
      paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
      paint.colorFilter = colorFilter
    }

    override fun setTintList(tint: ColorStateList?) {
      tintList = tint
    }

    override fun setState(stateSet: IntArray): Boolean {
      val result = super.setState(stateSet)
      applyTint()
      return result
    }

    @Deprecated("`Drawable#getOpacity` is deprecated.")
    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth() = intrinsicWidth

    override fun getIntrinsicHeight() = intrinsicHeight
  }
}
