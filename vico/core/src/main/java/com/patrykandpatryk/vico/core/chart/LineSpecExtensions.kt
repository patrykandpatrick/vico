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

package com.patrykandpatryk.vico.core.chart

import android.graphics.Paint
import com.patrykandpatryk.vico.core.chart.line.LineChart
import com.patrykandpatryk.vico.core.component.Component
import com.patrykandpatryk.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatryk.vico.core.component.text.TextComponent
import com.patrykandpatryk.vico.core.component.text.VerticalPosition
import com.patrykandpatryk.vico.core.formatter.ValueFormatter

/**
 * Creates a new [LineChart.LineSpec] based on this one, updating select properties.
 */
public fun LineChart.LineSpec.copy(
    lineColor: Int = this.lineColor,
    lineThicknessDp: Float = this.lineThicknessDp,
    lineBackgroundShader: DynamicShader? = this.lineBackgroundShader,
    lineCap: Paint.Cap = this.lineCap,
    point: Component? = this.point,
    pointSizeDp: Float = this.pointSizeDp,
    dataLabel: TextComponent? = this.dataLabel,
    dataLabelVerticalPosition: VerticalPosition = this.dataLabelVerticalPosition,
    dataLabelValueFormatter: ValueFormatter = this.dataLabelValueFormatter,
    dataLabelRotationDegrees: Float = this.dataLabelRotationDegrees,
    pointConnector: LineChart.LineSpec.PointConnector = this.pointConnector,
): LineChart.LineSpec = LineChart.LineSpec(
    lineColor = lineColor,
    lineThicknessDp = lineThicknessDp,
    lineBackgroundShader = lineBackgroundShader,
    lineCap = lineCap,
    point = point,
    pointSizeDp = pointSizeDp,
    dataLabel = dataLabel,
    dataLabelVerticalPosition = dataLabelVerticalPosition,
    dataLabelValueFormatter = dataLabelValueFormatter,
    dataLabelRotationDegrees = dataLabelRotationDegrees,
    pointConnector = pointConnector,
)
