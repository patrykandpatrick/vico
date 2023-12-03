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

package com.patrykandpatrick.vico.core.chart

import android.graphics.Paint
import com.patrykandpatrick.vico.core.chart.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.formatter.ValueFormatter

/**
 * Creates a new [LineCartesianLayer.LineSpec] based on this one, updating select properties.
 */
public fun LineCartesianLayer.LineSpec.copy(
    shader: DynamicShader = this.shader,
    thicknessDp: Float = this.thicknessDp,
    backgroundShader: DynamicShader? = this.backgroundShader,
    cap: Paint.Cap = this.cap,
    point: Component? = this.point,
    pointSizeDp: Float = this.pointSizeDp,
    dataLabel: TextComponent? = this.dataLabel,
    dataLabelVerticalPosition: VerticalPosition = this.dataLabelVerticalPosition,
    dataLabelValueFormatter: ValueFormatter = this.dataLabelValueFormatter,
    dataLabelRotationDegrees: Float = this.dataLabelRotationDegrees,
    pointConnector: LineCartesianLayer.LineSpec.PointConnector = this.pointConnector,
): LineCartesianLayer.LineSpec =
    LineCartesianLayer.LineSpec(
        shader = shader,
        thicknessDp = thicknessDp,
        backgroundShader = backgroundShader,
        cap = cap,
        point = point,
        pointSizeDp = pointSizeDp,
        dataLabel = dataLabel,
        dataLabelVerticalPosition = dataLabelVerticalPosition,
        dataLabelValueFormatter = dataLabelValueFormatter,
        dataLabelRotationDegrees = dataLabelRotationDegrees,
        pointConnector = pointConnector,
    )
