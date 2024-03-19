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

package com.patrykandpatrick.vico.core.common.extension

import android.graphics.Path
import com.patrykandpatrick.vico.core.common.Point

internal fun Path.horizontalCubicTo(
    prevX: Float,
    prevY: Float,
    x: Float,
    y: Float,
    curvature: Float,
) {
    val directionMultiplier = if (x >= prevX) 1f else -1f
    cubicTo(prevX + directionMultiplier * curvature, prevY, x - directionMultiplier * curvature, y, x, y)
}

internal fun Path.moveTo(point: Point) {
    moveTo(point.x, point.y)
}
