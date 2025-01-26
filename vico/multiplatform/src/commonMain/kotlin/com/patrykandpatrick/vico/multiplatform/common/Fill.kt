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

package com.patrykandpatrick.vico.multiplatform.common

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import com.patrykandpatrick.vico.multiplatform.common.data.CacheStore

/**
 * Stores fill properties.
 *
 * @property color the color. If [brush] is not null, this is [Color.Black].
 * @property brush the [Brush].
 */
public class Fill private constructor(public val color: Color, public val brush: Brush?) {
  /** Creates a color [Fill]. */
  public constructor(color: Color) : this(color = color, brush = null)

  /** Creates a [Brush]&#0020;[Fill]. */
  public constructor(brush: Brush) : this(Color.Black, brush)

  override fun equals(other: Any?): Boolean =
    this === other || other is Fill && color == other.color && brush == other.brush

  override fun hashCode(): Int = 31 * color.toArgb() + brush?.hashCode().orZero

  /** Houses [Fill] singletons. */
  public companion object {
    /** A black [Fill]. */
    public val Black: Fill = Fill(Color.Black)

    /** A transparent [Fill]. */
    public val Transparent: Fill = Fill(Color.Transparent)
  }
}

private val paint = Paint()
private val cacheKeyNamespace = CacheStore.KeyNamespace()

internal fun Fill.extractColor(
  context: DrawingContext,
  width: Float,
  height: Float,
  side: Int = 1,
): Color =
  if (brush != null) {
    val (bitmap, canvas) = context.getBitmap(cacheKeyNamespace)
    val correctedHeight = if (height <= 0f) bitmap.height.toFloat() else height.coerceAtLeast(1f)
    val correctedWidth = if (width <= 0f) bitmap.width.toFloat() else width.coerceAtLeast(1f)
    brush.applyTo(size = Size(correctedWidth, correctedHeight), p = paint, alpha = 1f)
    canvas.drawRect(0f, 0f, correctedWidth, correctedHeight, paint)
    bitmap.getPixel(
      correctedWidth.half.toInt(),
      if (side == 1) 0 else (correctedHeight - 1).toInt(),
    )
  } else {
    color
  }

/** Creates a [Fill]. */
public fun fill(color: Color): Fill = Fill(color)

/** Creates a [Fill]. */
public fun fill(brush: Brush): Fill = Fill(brush)
