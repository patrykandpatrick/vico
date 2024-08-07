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

import android.graphics.RectF
import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.common.data.CacheStore
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore

/** A [MeasuringContext] implementation that facilitates the mutation of some of its properties. */
public open class MutableMeasuringContext(
  override val canvasBounds: RectF,
  override var density: Float,
  override var isLtr: Boolean,
  @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) public var spToPx: (Float) -> Float,
) : MeasuringContext {
  @Deprecated(
    "To cache drawing data, use `cacheStore`. If using `extraStore` for communication between " +
      "functions or classes, switch to a suitable alternative."
  )
  override val extraStore: MutableExtraStore = MutableExtraStore()

  override val cacheStore: CacheStore = CacheStore()

  override fun spToPx(sp: Float): Float = spToPx.invoke(sp)
}
