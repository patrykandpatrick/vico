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

package com.patrykandpatryk.vico.sample.util

internal object Tokens {

    object ColumnChart {
        const val PERSISTENT_MARKER_X = 5f
    }

    object LineChart {
        const val THRESHOLD_VALUE = 7f
        const val THRESHOLD_LINE_STROKE_WIDTH_DP = 2f
    }

    object ComposedChart {
        const val SHADER_ALPHA = 0.16f
    }

    object GroupedColumnChart {
        const val THRESHOLD_START = 7f
        const val THRESHOLD_END = 12f
        const val THRESHOLD_LINE_BACKGROUND_ALPHA = 0.16f
    }

    object LineChartWithLabelsInside {
        const val LABEL_VERTICAL_MARGIN_DP = 4f
    }

    object StackedColumnChart {
        const val AXIS_LABEL_ROTATION_DEGREES = 45f
    }
}
