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

package com.patrykandpatrick.vico.core.cartesian.layer

import androidx.compose.runtime.Immutable

/**
 * Stores [CartesianLayer] padding sizes. Scalable padding depends on the zoom factor.
 *
 * @property scalableStartDp the size of the scalable start padding (in dp).
 * @property scalableEndDp the size of the scalable end padding (in dp).
 * @property unscalableStartDp the size of the unscalable start padding (in dp).
 * @property unscalableEndDp the size of the unscalable end padding (in dp).
 */
@Immutable
public class CartesianLayerPadding(
  public val scalableStartDp: Float = 0f,
  public val scalableEndDp: Float = 0f,
  public val unscalableStartDp: Float = 0f,
  public val unscalableEndDp: Float = 0f,
) {
  override fun equals(other: Any?): Boolean =
    this === other ||
      other is CartesianLayerPadding &&
        scalableStartDp == other.scalableStartDp &&
        scalableEndDp == other.scalableEndDp &&
        unscalableStartDp == other.unscalableStartDp &&
        unscalableEndDp == other.unscalableEndDp

  override fun hashCode(): Int {
    var result = scalableStartDp.hashCode()
    result = 31 * result + scalableEndDp.hashCode()
    result = 31 * result + unscalableStartDp.hashCode()
    result = 31 * result + unscalableEndDp.hashCode()
    return result
  }
}
