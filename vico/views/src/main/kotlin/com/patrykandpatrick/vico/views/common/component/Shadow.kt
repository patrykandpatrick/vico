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

package com.patrykandpatrick.vico.views.common.component

import android.graphics.Paint
import com.patrykandpatrick.vico.views.common.Defaults
import com.patrykandpatrick.vico.views.common.MeasuringContext

/**
 * Stores shadow properties.
 *
 * @param radiusDp the blur radius (in dp).
 * @param xDp the horizontal offset (in dp).
 * @param yDp the vertical offset (in dp).
 * @param color the color.
 */
public data class Shadow(
  private val radiusDp: Float,
  private val xDp: Float = 0f,
  private val yDp: Float = 0f,
  private val color: Int = Defaults.SHADOW_COLOR,
) {
  /** Updates [paint]’s shadow layer. */
  public fun updateShadowLayer(context: MeasuringContext, paint: Paint) {
    with(context) { paint.setShadowLayer(radiusDp.pixels, xDp.pixels, yDp.pixels, color) }
  }
}
