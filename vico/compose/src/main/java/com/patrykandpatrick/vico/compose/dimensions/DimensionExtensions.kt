/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.dimensions

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions

/**
 * Creates a [MutableDimensions] instance with a common value for each coordinate.
 */
public fun dimensionsOf(all: Dp): MutableDimensions = dimensionsOf(
    start = all,
    top = all,
    end = all,
    bottom = all,
)

/**
 * Creates a [MutableDimensions] instance using the provided measurements.
 */
public fun dimensionsOf(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
): MutableDimensions = MutableDimensions(
    startDp = horizontal.value,
    topDp = vertical.value,
    endDp = horizontal.value,
    bottomDp = vertical.value,
)

/**
 * Creates a [MutableDimensions] instance using the provided measurements.
 */
public fun dimensionsOf(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
): MutableDimensions = MutableDimensions(
    startDp = start.value,
    topDp = top.value,
    endDp = end.value,
    bottomDp = bottom.value,
)
