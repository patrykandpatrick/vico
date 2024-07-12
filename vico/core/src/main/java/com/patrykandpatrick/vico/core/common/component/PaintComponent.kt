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

package com.patrykandpatrick.vico.core.common.component

import android.graphics.Paint
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.DrawContext

/** A base class for components that use [android.graphics.Paint] to draw themselves. */
@Suppress("UNCHECKED_CAST")
public open class PaintComponent<C> protected constructor() {
  protected val componentShadow: ComponentShadow = ComponentShadow()

  /** Updates the shadow layer. */
  protected fun updateShadowLayer(context: DrawContext, paint: Paint, opacity: Float = 1f) {
    componentShadow.updateShadowLayer(context, paint, opacity)
  }

  /**
   * Applies a drop shadow.
   *
   * @param radius the blur radius.
   * @param dx the horizontal offset.
   * @param dy the vertical offset.
   * @param color the shadow color.
   */
  public fun setShadow(
    radius: Float,
    dx: Float = 0f,
    dy: Float = 0f,
    color: Int = Defaults.SHADOW_COLOR,
  ): C =
    apply {
      componentShadow.apply {
        this.radius = radius
        this.dx = dx
        this.dy = dy
        this.color = color
      }
    }
      as C

  /** Removes this [ShapeComponent]’s drop shadow. */
  public fun clearShadow(): C =
    apply {
      componentShadow.apply {
        this.radius = 0f
        this.dx = 0f
        this.dy = 0f
        this.color = 0
      }
    }
      as C
}
