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

package pl.patrykgoworowski.vico.core

import android.graphics.Color
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.extension.copyColor

val DEF_LABEL_COMPONENT: TextComponent
    get() = TextComponent()
        .apply {
            setPadding(start = 4f, top = 2f, end = 4f, bottom = 2f)
        }

val DEF_AXIS_COMPONENT: LineComponent
    get() = LineComponent(Color.BLACK.copyColor(alpha = 128), 2f)

val DEF_TICK_COMPONENT: LineComponent
    get() = LineComponent(Color.BLACK.copyColor(alpha = 128), 2f)

val DEF_GUIDELINE_COMPONENT: LineComponent
    get() = LineComponent(Color.BLACK.copyColor(alpha = 64), 1f)

const val DEF_LABEL_LINE_COUNT = 1
const val DEF_LABEL_COUNT = 99
const val DEF_LABEL_SIZE = 12f
const val DEF_LABEL_SPACING = 16f

const val DEF_MARKER_TICK_SIZE = 6f

const val MAX_ZOOM = 10f
const val MIN_ZOOM = 0.1f

public const val DEF_SHADOW_COLOR: Int = 0x8A000000.toInt()

object Dimens {
    const val AXIS_LABEL_HORIZONTAL_PADDING = 4
    const val AXIS_LABEL_MAX_LINES = 1
    const val AXIS_LABEL_VERTICAL_PADDING = 2
    const val AXIS_LABEL_SIZE = 12

    const val AXIS_GUIDELINE_WIDTH = 1f
    const val AXIS_LINE_WIDTH = 1f
    const val AXIS_TICK_LENGTH = 4f

    const val COLUMN_WIDTH = 8
    const val COLUMN_INSIDE_SPACING = 8
    const val COLUMN_OUTSIDE_SPACING = 32
    const val COLUMN_ROUNDNESS_PERCENT = 40

    const val CUBIC_STRENGTH = 1f

    const val DASH_LENGTH = 4
    const val DASH_GAP = 2

    const val LINE_THICKNESS = 2f

    const val MARKER_INDICATOR_SIZE = 36f
    const val MARKER_HORIZONTAL_PADDING = 8f
    const val MARKER_VERTICAL_PADDING = 4f

    const val POINT_SIZE = 16f
    const val POINT_SPACING = 16f
}

@Suppress("MagicNumber")
interface Colors {
    val axisLabelColor: Long
    val axisGuidelineColor: Long
    val axisLineColor: Long

    val column1Color: Long
    val column2Color: Long
    val column3Color: Long

    val lineColor: Long

    object Light : Colors {
        override val axisLabelColor: Long = 0xDE000000
        override val axisGuidelineColor: Long = 0xFFAAAAAA
        override val axisLineColor: Long = 0xFF8A8A8A

        override val column1Color: Long = 0xFF787878
        override val column2Color: Long = 0xFF5A5A5A
        override val column3Color: Long = 0xFF383838

        override val lineColor: Long = 0xFF1A1A1A
    }

    object Dark : Colors {
        override val axisLabelColor: Long = 0xFFFFFFFF
        override val axisGuidelineColor: Long = 0xFF424242
        override val axisLineColor: Long = 0xFF555555

        override val column1Color: Long = 0xFFCACACA
        override val column2Color: Long = 0xFFA8A8A8
        override val column3Color: Long = 0xFF888888

        override val lineColor: Long = 0xFFEFEFEF
    }
}
