@file:Suppress("Unused")

/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.view.component.shape

import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.component.shape.corner.Corner
import pl.patrykgoworowski.vico.core.component.shape.corner.CorneredShape
import pl.patrykgoworowski.vico.core.component.shape.corner.CutCornerTreatment
import pl.patrykgoworowski.vico.core.component.shape.corner.RoundedCornerTreatment

fun Shapes.roundedCornersShape(all: Float): Shape =
    roundedCornersShape(all, all, all, all)

fun Shapes.roundedCornersShape(
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

fun Shapes.cutCornerShape(all: Float): Shape =
    cutCornerShape(all, all, all, all)

fun Shapes.cutCornerShape(
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomRight: Float = 0f,
    bottomLeft: Float = 0f
): CorneredShape = CorneredShape(
    Corner.Absolute(topLeft, CutCornerTreatment),
    Corner.Absolute(topRight, CutCornerTreatment),
    Corner.Absolute(bottomRight, CutCornerTreatment),
    Corner.Absolute(bottomLeft, CutCornerTreatment),
)
