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

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.cartesian.CartesianLayerPadding

/** Creates a [CartesianLayerPadding] instance. */
public fun cartesianLayerPadding(
  scalableStart: Dp = 0.dp,
  scalableEnd: Dp = 0.dp,
  unscalableStart: Dp = 0.dp,
  unscalableEnd: Dp = 0.dp,
): CartesianLayerPadding =
  CartesianLayerPadding(
    scalableStart.value,
    scalableEnd.value,
    unscalableStart.value,
    unscalableEnd.value,
  )
