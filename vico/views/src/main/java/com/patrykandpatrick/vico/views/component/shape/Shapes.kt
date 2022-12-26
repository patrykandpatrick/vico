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

@file:Suppress("Unused")

package com.patrykandpatrick.vico.views.component.shape

import com.patrykandpatrick.vico.core.component.shape.Shape
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.cornered.Corner
import com.patrykandpatrick.vico.core.component.shape.cornered.CorneredShape
import com.patrykandpatrick.vico.core.component.shape.cornered.CutCornerTreatment
import com.patrykandpatrick.vico.core.component.shape.cornered.RoundedCornerTreatment

/**
 * Creates a [CorneredShape] with rounded corners of the provided size.
 */
public fun Shapes.roundedCornerShape(all: Float): Shape =
    roundedCornerShape(all, all, all, all)

/**
 * Creates a [CorneredShape] with rounded corners of the provided sizes.
 */
public fun Shapes.roundedCornerShape(
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomRight: Float = 0f,
    bottomLeft: Float = 0f,
): CorneredShape = CorneredShape(
    Corner.Absolute(topLeft, RoundedCornerTreatment),
    Corner.Absolute(topRight, RoundedCornerTreatment),
    Corner.Absolute(bottomRight, RoundedCornerTreatment),
    Corner.Absolute(bottomLeft, RoundedCornerTreatment),
)

/**
 * Creates a [CorneredShape] with cut corners of the provided size.
 */
public fun Shapes.cutCornerShape(all: Float): Shape =
    cutCornerShape(all, all, all, all)

/**
 * Creates a [CorneredShape] with cut corners of the provided sizes.
 */
public fun Shapes.cutCornerShape(
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomRight: Float = 0f,
    bottomLeft: Float = 0f,
): CorneredShape = CorneredShape(
    Corner.Absolute(topLeft, CutCornerTreatment),
    Corner.Absolute(topRight, CutCornerTreatment),
    Corner.Absolute(bottomRight, CutCornerTreatment),
    Corner.Absolute(bottomLeft, CutCornerTreatment),
)
