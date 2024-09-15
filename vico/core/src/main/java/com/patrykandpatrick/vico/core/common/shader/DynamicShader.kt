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

package com.patrykandpatrick.vico.core.common.shader

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.BlendMode
import android.graphics.ComposeShader
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import com.patrykandpatrick.vico.core.common.DrawingContext
import com.patrykandpatrick.vico.core.common.component.Component

/** Creates [Shader]s on demand. */
public fun interface DynamicShader {
  /** Creates a [Shader] by using the provided [bounds]. */
  public fun provideShader(context: DrawingContext, bounds: RectF): Shader =
    provideShader(
      context = context,
      left = bounds.left,
      top = bounds.top,
      right = bounds.right,
      bottom = bounds.bottom,
    )

  /** Creates a [Shader] by using the provided [left], [top], [right], and [bottom] bounds. */
  public fun provideShader(
    context: DrawingContext,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ): Shader

  public companion object {
    /** Creates a [DynamicShader] out of the given [bitmap]. */
    public fun bitmap(
      bitmap: Bitmap,
      tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
      tileYMode: Shader.TileMode = tileXMode,
    ): DynamicShader =
      object : CacheableDynamicShader() {
        override fun createShader(
          context: DrawingContext,
          left: Float,
          top: Float,
          right: Float,
          bottom: Float,
        ): Shader = BitmapShader(bitmap, tileXMode, tileYMode)
      }

    /**
     * Creates a [ComposeShader] out of two [DynamicShader]s, combining [first] and [second] via
     * [mode].
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun compose(
      first: DynamicShader,
      second: DynamicShader,
      mode: BlendMode,
    ): DynamicShader = DynamicShader { context, left, top, right, bottom ->
      ComposeShader(
        first.provideShader(context, left, top, right, bottom),
        second.provideShader(context, left, top, right, bottom),
        mode,
      )
    }

    /**
     * Creates a [ComposeShader] out of two [DynamicShader]s, combining [first] and [second] via
     * [mode].
     */
    public fun compose(
      first: DynamicShader,
      second: DynamicShader,
      mode: PorterDuff.Mode,
    ): DynamicShader = DynamicShader { context, left, top, right, bottom ->
      ComposeShader(
        first.provideShader(context, left, top, right, bottom),
        second.provideShader(context, left, top, right, bottom),
        mode,
      )
    }

    /**
     * Creates a [DynamicShader] in the form of a horizontal gradient.
     *
     * @param colors the sRGB colors to be distributed along the gradient line.
     */
    public fun horizontalGradient(vararg colors: Int): DynamicShader = horizontalGradient(colors)

    /**
     * Creates a [DynamicShader] in the form of a horizontal gradient.
     *
     * @param colors the sRGB colors to be distributed along the gradient line.
     * @param positions controls the position of each color on the gradient line. Each element of
     *   the array should belong to the interval [[0, 1]], where 0 corresponds to the start of the
     *   gradient line, and 1 corresponds to the end of the gradient line. If `null` (the default
     *   value) is passed, the colors will be distributed evenly along the gradient line.
     */
    public fun horizontalGradient(colors: IntArray, positions: FloatArray? = null): DynamicShader =
      LinearGradientShader(colors, positions, true)

    /**
     * Creates a [DynamicShader] in the form of a vertical gradient.
     *
     * @param colors the sRGB colors to be distributed along the gradient line.
     */
    public fun verticalGradient(vararg colors: Int): DynamicShader = verticalGradient(colors)

    /**
     * Creates a [DynamicShader] in the form of a vertical gradient.
     *
     * @param colors the sRGB colors to be distributed along the gradient line.
     * @param positions controls the position of each color on the gradient line. Each element of
     *   the array should belong to the interval [[0, 1]], where 0 corresponds to the start of the
     *   gradient line, and 1 corresponds to the end of the gradient line. If `null` (the default
     *   value) is passed, the colors will be distributed evenly along the gradient line.
     */
    public fun verticalGradient(colors: IntArray, positions: FloatArray? = null): DynamicShader =
      LinearGradientShader(colors, positions, false)

    /**
     * Creates a [DynamicShader] that repeatedly draws [component] in a grid or checkered pattern.
     */
    public fun component(
      component: Component,
      componentSizeDp: Float,
      checkeredArrangement: Boolean = true,
      tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
      tileYMode: Shader.TileMode = tileXMode,
    ): DynamicShader =
      ComponentShader(component, componentSizeDp, checkeredArrangement, tileXMode, tileYMode)
  }
}

/** Converts this [Shader] to a [DynamicShader]. */
public fun Shader.toDynamicShader(): DynamicShader = DynamicShader { _, _, _, _, _ -> this }
