/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.core.chart

import android.graphics.Paint
import com.patrykandpatryk.vico.core.chart.line.LineChart
import com.patrykandpatryk.vico.core.component.Component
import com.patrykandpatryk.vico.core.component.shape.shader.DynamicShader

/**
 * Creates a new [LineChart.LineSpec] based on this one, updating select properties.
 */
public fun LineChart.LineSpec.copy(
    lineColor: Int = this.lineColor,
    lineThicknessDp: Float = this.lineThicknessDp,
    lineBackgroundShader: DynamicShader? = this.lineBackgroundShader,
    lineCap: Paint.Cap = this.lineCap,
    cubicStrength: Float = this.cubicStrength,
    point: Component? = this.point,
    pointSizeDp: Float = this.pointSizeDp,
): LineChart.LineSpec = LineChart.LineSpec(
    lineColor = lineColor,
    lineThicknessDp = lineThicknessDp,
    lineBackgroundShader = lineBackgroundShader,
    lineCap = lineCap,
    cubicStrength = cubicStrength,
    point = point,
    pointSizeDp = pointSizeDp,
)
