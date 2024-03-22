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

package com.patrykandpatrick.vico.core.cartesian.layer

import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.common.component.Component

internal fun Component.drawPoint(
    context: CartesianDrawContext,
    x: Float,
    y: Float,
    halfPointSize: Float,
) {
    draw(
        context = context,
        left = x - halfPointSize,
        top = y - halfPointSize,
        right = x + halfPointSize,
        bottom = y + halfPointSize,
    )
}

internal inline val LineCartesianLayer.LineSpec.pointSizeDpOrZero: Float
    get() = if (point != null) pointSizeDp else 0f
