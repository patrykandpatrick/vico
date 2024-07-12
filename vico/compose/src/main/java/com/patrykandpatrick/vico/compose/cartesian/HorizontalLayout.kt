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
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout

/** Returns a [HorizontalLayout.Segmented] instance. */
public fun HorizontalLayout.Companion.segmented(): HorizontalLayout.Segmented =
  HorizontalLayout.Segmented

/** Creates a [HorizontalLayout.FullWidth] instance. */
public fun HorizontalLayout.Companion.fullWidth(
  scalableStartPadding: Dp = 0.dp,
  scalableEndPadding: Dp = 0.dp,
  unscalableStartPadding: Dp = 0.dp,
  unscalableEndPadding: Dp = 0.dp,
): HorizontalLayout.FullWidth =
  HorizontalLayout.FullWidth(
    scalableStartPadding.value,
    scalableEndPadding.value,
    unscalableStartPadding.value,
    unscalableEndPadding.value,
  )
