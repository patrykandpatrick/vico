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

package com.patrykandpatrick.vico.core.cartesian.data

import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.common.data.CartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.gcdWith

/** Stores a [CartesianChart]’s data. */
public class CartesianChartModel {
  /** The [CartesianLayerModel]s. */
  public val models: List<CartesianLayerModel>

  /** Identifies this [CartesianChartModel] in terms of the [CartesianLayerModel.id]s. */
  public val id: Int

  /**
   * Expresses the size of this [CartesianChartModel] in terms of the range of the _x_ values
   * covered.
   */
  public val width: Double

  /** Stores auxiliary data, including [CartesianLayerDrawingModel]s. */
  public val extraStore: ExtraStore

  /** Creates a [CartesianChartModel] consisting of the given [CartesianLayerModel]s. */
  public constructor(models: List<CartesianLayerModel>) : this(models, ExtraStore.Empty)

  /** Creates a [CartesianChartModel] consisting of the given [CartesianLayerModel]s. */
  public constructor(vararg models: CartesianLayerModel) : this(models.toList())

  internal constructor(
    models: List<CartesianLayerModel>,
    extraStore: ExtraStore,
  ) : this(
    models = models,
    id = models.map { it.id }.hashCode(),
    width = models.maxOf { it.maxX } - models.minOf { it.minX },
    extraStore = extraStore,
  )

  internal constructor(
    models: List<CartesianLayerModel>,
    id: Int,
    width: Double,
    extraStore: ExtraStore,
  ) {
    this.models = models
    this.id = id
    this.width = width
    this.extraStore = extraStore
  }

  /** Returns the greatest common divisor of the _x_ values’ differences. */
  public fun getXDeltaGcd(): Double =
    models.fold<CartesianLayerModel, Double?>(null) { gcd, layerModel ->
      val layerModelGcd = layerModel.getXDeltaGcd()
      gcd?.gcdWith(layerModelGcd) ?: layerModelGcd
    } ?: 1.0

  /**
   * Creates a copy of this [CartesianChartModel] with the given [ExtraStore], which is also applied
   * to the [CartesianLayerModel]s.
   */
  public fun copy(extraStore: ExtraStore): CartesianChartModel =
    CartesianChartModel(models.map { it.copy(extraStore) }, id, width, extraStore)

  /** Creates an immutable copy of this [CartesianChartModel]. */
  public fun toImmutable(): CartesianChartModel = this

  public companion object {
    /** An empty [CartesianChartModel]. */
    public val Empty: CartesianChartModel =
      CartesianChartModel(models = emptyList(), id = 0, width = 0.0, extraStore = ExtraStore.Empty)
  }
}
