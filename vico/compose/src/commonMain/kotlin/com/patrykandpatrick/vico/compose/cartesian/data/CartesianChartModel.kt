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

package com.patrykandpatrick.vico.compose.cartesian.data

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.common.Animation
import com.patrykandpatrick.vico.compose.common.gcdWith

/**
 * Aggregates the [CartesianLayerModel]s of a [CartesianChart]’s [CartesianLayer]s. This is created
 * by the library and is read-only.
 */
public class CartesianChartModel internal constructor(models: List<CartesianLayerModel>) {
  /** The [CartesianLayerModel]s, in [CartesianLayer] order. */
  public val models: List<CartesianLayerModel> = models.toList()

  /**
   * Expresses the size of this [CartesianChartModel] in terms of the range of the _x_-values
   * covered.
   */
  public val width: Double =
    models
      .filterNot { it.isEmpty }
      .let { nonEmpty ->
        if (nonEmpty.isEmpty()) 0.0 else nonEmpty.maxOf { it.maxX } - nonEmpty.minOf { it.minX }
      }

  /**
   * Returns the greatest common divisor of the _x_-values’ differences, or 1 if there are no
   * entries. Empty [CartesianLayerModel]s are ignored.
   */
  public fun getXDeltaGcd(): Double =
    models
      .filterNot { it.isEmpty }
      .fold<CartesianLayerModel, Double?>(null) { gcd, layerModel ->
        val layerModelGcd = layerModel.getXDeltaGcd()
        gcd?.gcdWith(layerModelGcd) ?: layerModelGcd
      } ?: 1.0

  override fun equals(other: Any?): Boolean =
    this === other || other is CartesianChartModel && models == other.models

  override fun hashCode(): Int = models.hashCode()

  override fun toString(): String = "CartesianChartModel(models=$models, width=$width)"
}

internal val defaultCartesianDiffAnimationSpec: AnimationSpec<Float> =
  tween(durationMillis = Animation.DIFF_DURATION)
