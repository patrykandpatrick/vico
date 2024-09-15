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

package com.patrykandpatrick.vico.compose.common

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.common.Dimensions

/** Creates a [Dimensions] instance with a common value for each coordinate. */
public fun dimensions(all: Dp = 0.dp): Dimensions = Dimensions(all.value)

/** Creates a [Dimensions] instance using the provided measurements. */
public fun dimensions(horizontal: Dp = 0.dp, vertical: Dp = 0.dp): Dimensions =
  Dimensions(horizontal.value, vertical.value)

/** Creates a [Dimensions] instance using the provided measurements. */
public fun dimensions(
  start: Dp = 0.dp,
  top: Dp = 0.dp,
  end: Dp = 0.dp,
  bottom: Dp = 0.dp,
): Dimensions = Dimensions(start.value, top.value, end.value, bottom.value)
