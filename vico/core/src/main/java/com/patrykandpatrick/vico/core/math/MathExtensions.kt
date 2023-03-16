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

import com.patrykandpatrick.vico.core.extension.PI_RAD
import kotlin.math.cos
import kotlin.math.sin

/**
 * Translates [this] x-axis coordinate by the given [angle].
 */
public fun Float.translateXByAngle(angle: Float): Float =
    (this * cos(angle * Math.PI / PI_RAD)).toFloat()

/**
 * Translates [this] y-axis coordinate by the given [angle].
 */
public fun Float.translateYByAngle(angle: Float): Float =
    (this * sin(angle * Math.PI / PI_RAD)).toFloat()
