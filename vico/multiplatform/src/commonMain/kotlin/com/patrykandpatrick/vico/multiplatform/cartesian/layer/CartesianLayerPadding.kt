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

package com.patrykandpatrick.vico.multiplatform.cartesian.layer

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Stores [CartesianLayer] padding sizes. Scalable padding depends on the zoom factor.
 *
 * @property scalableStart the size of the scalable start padding.
 * @property scalableEnd the size of the scalable end padding.
 * @property unscalableStart the size of the unscalable start padding.
 * @property unscalableEnd the size of the unscalable end padding.
 */
@Immutable
public class CartesianLayerPadding(
  public val scalableStart: Dp = 0f.dp,
  public val scalableEnd: Dp = 0f.dp,
  public val unscalableStart: Dp = 0f.dp,
  public val unscalableEnd: Dp = 0f.dp,
) {
  override fun equals(other: Any?): Boolean =
    this === other ||
      other is CartesianLayerPadding &&
        scalableStart == other.scalableStart &&
        scalableEnd == other.scalableEnd &&
        unscalableStart == other.unscalableStart &&
        unscalableEnd == other.unscalableEnd

  override fun hashCode(): Int {
    var result = scalableStart.hashCode()
    result = 31 * result + scalableEnd.hashCode()
    result = 31 * result + unscalableStart.hashCode()
    result = 31 * result + unscalableEnd.hashCode()
    return result
  }
}
