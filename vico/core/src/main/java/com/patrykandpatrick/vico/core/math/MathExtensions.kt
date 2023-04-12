/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.math

import com.patrykandpatrick.vico.core.model.Point
import kotlin.math.cos
import kotlin.math.sin

/**
 * Translates given [point] by the given [angle] (in radians) relative to the [center].
 */
public fun translatePointByAngle(center: Point, point: Point, angle: Double): Point =
    Point(
        ((point.x - center.x) * cos(angle) - (point.y - center.y) * sin(angle) + center.x).toFloat(),
        ((point.y - center.y) * cos(angle) + (point.x - center.x) * sin(angle) + center.y).toFloat(),
    )
