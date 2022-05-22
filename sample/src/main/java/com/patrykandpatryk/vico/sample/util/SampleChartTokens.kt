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

@Suppress("MagicNumber")
internal object SampleChartTokens {
    object ColumnChart {
        val entityColors = longArrayOf(0xFFFF6F3C)
        const val THRESHOLD_VALUE = 13f
        const val THRESHOLD_LINE_COLOR = 0xFF3EC1D3
        const val THRESHOLD_LINE_STROKE_WIDTH_DP = 2f
        const val THRESHOLD_LINE_PADDING_DP = 4f
        const val THRESHOLD_LINE_MARGINS_DP = 4f
    }
    object LineChart {
        val entityColors = longArrayOf(0xFFAA96DA)
        const val PERSISTENT_MARKER_X = 6f
    }
    object ComposedChart {
        val entityColors = longArrayOf(0xFF3D84A8, 0xFF46CDCF, 0xFFABEDD8)
    }
    object StackedColumnChart {
        val entityColors = longArrayOf(0xFF6639A6, 0xFF3490DE, 0xFF6FE7DD)
        const val AXIS_LABEL_ROTATION_DEGREES = 45f
        const val MAX_LABEL_COUNT = 2
    }
    object GroupedColumnChart {
        val entityColors = longArrayOf(0xFF68A7AD, 0xFF99C4C8, 0xFFE5CB9F)
        const val THRESHOLD_RANGE_START = 7f
        const val THRESHOLD_RANGE_END = 14f
        const val THRESHOLD_LINE_COLOR = 0xFF68A7AD
        const val THRESHOLD_LINE_ALPHA = 0.16f
        const val THRESHOLD_LINE_PADDING_DP = 4f
        const val THRESHOLD_LINE_MARGINS_DP = 4f
    }
    object LineChartWithLabelsInside {
        val entityColors = longArrayOf(0xFFB983FF, 0xFF94B3FD, 0xFF94DAFF)
        const val LABEL_BACKGROUND_COLOR = 0xFFFABB51
        const val LABEL_PADDING_VERTICAL_DP = 4f
        const val LABEL_PADDING_HORIZONTAL_DP = 8f
        const val LABEL_MARGIN_DP = 4f
    }
}
