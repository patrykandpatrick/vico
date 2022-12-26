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

package com.patrykandpatrick.vico.core.chart.line

import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.context.DrawContext

internal fun Component.drawPoint(
    context: DrawContext,
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

internal inline val LineChart.LineSpec.pointSizeDpOrZero: Float
    get() = if (point != null) pointSizeDp else 0f
