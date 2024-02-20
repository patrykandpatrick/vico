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

package com.patrykandpatrick.vico.core

import androidx.annotation.RestrictTo

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public object Animation {
    public val range: ClosedFloatingPointRange<Float> = 0f..1f
    public const val DIFF_DURATION: Int = 500
    public const val ANIMATED_SCROLL_DURATION: Int = 300
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public object Defaults {
    public const val AXIS_LABEL_HORIZONTAL_PADDING: Int = 4
    public const val AXIS_LABEL_MAX_LINES: Int = 1
    public const val AXIS_LABEL_VERTICAL_PADDING: Int = 2
    public const val AXIS_LABEL_SIZE: Int = 12
    public const val AXIS_LABEL_HORIZONTAL_MARGIN: Int = 0
    public const val AXIS_LABEL_VERTICAL_MARGIN: Int = 0
    public const val AXIS_LABEL_ROTATION_DEGREES: Float = 0f
    public const val AXIS_GUIDELINE_WIDTH: Float = 1f
    public const val AXIS_LINE_WIDTH: Float = 1f
    public const val AXIS_TICK_LENGTH: Float = 4f
    public const val COLUMN_WIDTH: Float = 8f
    public const val COLUMN_INSIDE_SPACING: Float = 8f
    public const val COLUMN_OUTSIDE_SPACING: Float = 32f
    public const val COLUMN_ROUNDNESS_PERCENT: Int = 40
    public const val CUBIC_STRENGTH: Float = 1f
    public const val DASH_LENGTH: Float = 4f
    public const val DASH_GAP: Float = 2f
    public const val FADING_EDGE_VISIBILITY_THRESHOLD_DP: Float = 16f
    public const val FADING_EDGE_WIDTH_DP: Float = 32f
    public const val LABEL_LINE_COUNT: Int = 1
    public const val LINE_THICKNESS: Float = 2f
    public const val MARKER_INDICATOR_SIZE: Float = 36f
    public const val MARKER_HORIZONTAL_PADDING: Float = 8f
    public const val MARKER_VERTICAL_PADDING: Float = 4f
    public const val MARKER_TICK_SIZE: Float = 6f
    public const val MAX_ZOOM: Float = 10f
    public const val MIN_ZOOM: Float = 0.1f
    public const val MAX_LABEL_COUNT: Int = 100
    public const val POINT_SIZE: Float = 16f
    public const val POINT_SPACING: Float = 32f
    public const val TEXT_COMPONENT_TEXT_SIZE: Float = 12f
    public const val THRESHOLD_LINE_THICKNESS: Float = 2f
    public const val SHADOW_COLOR: Int = 0x8A000000.toInt()
    public const val CHART_HEIGHT: Float = 200f
}

/**
 * The default chart colors.
 */
public interface DefaultColors {
    /**
     * The default color for elevation overlays. Its opacity is modified depending on the elevation.
     */
    public val elevationOverlayColor: Long

    /**
     * The default color for axis labels.
     */
    public val axisLabelColor: Long

    /**
     * The default color for axis guidelines.
     */
    public val axisGuidelineColor: Long

    /**
     * The default color for axis lines.
     */
    public val axisLineColor: Long

    /**
     * The color for columns whose index in a column collection is 3k (k ∈ N)
     * and for lines whose index in the list of lines in a line chart is 3k (k ∈ N).
     */
    public val entity1Color: Long

    /**
     * The color for columns whose index in a column collection is 1 + 3k (k ∈ N)
     * and for lines whose index in the list of lines in a line chart is 1 + 3k (k ∈ N).
     */
    public val entity2Color: Long

    /**
     * The color for columns whose index in a column collection is 2 + 3k (k ∈ N)
     * and for lines whose index in the list of lines in a line chart is 2 + 3k (k ∈ N).
     */
    public val entity3Color: Long

    /**
     * The default line color for line charts.
     */
    public val lineColor: Long

    /**
     * The default chart colors for light mode.
     */
    public object Light : DefaultColors {
        override val elevationOverlayColor: Long = 0x00000000

        override val axisLabelColor: Long = 0xDE000000
        override val axisGuidelineColor: Long = 0x47000000
        override val axisLineColor: Long = 0x47000000

        override val entity1Color: Long = 0xFF787878
        override val entity2Color: Long = 0xFF5A5A5A
        override val entity3Color: Long = 0xFF383838

        override val lineColor: Long = 0xFF1A1A1A
    }

    /**
     * The default chart colors for dark mode.
     */
    public object Dark : DefaultColors {
        override val elevationOverlayColor: Long = 0xFFFFFFFF

        override val axisLabelColor: Long = 0xFFFFFFFF
        override val axisGuidelineColor: Long = 0xFF424242
        override val axisLineColor: Long = 0xFF555555

        override val entity1Color: Long = 0xFFCACACA
        override val entity2Color: Long = 0xFFA8A8A8
        override val entity3Color: Long = 0xFF888888

        override val lineColor: Long = 0xFFEFEFEF
    }
}

/**
 * Default alpha values.
 */
public object DefaultAlpha {
    /**
     * The default value for alpha on the start of line’s background gradient.
     */
    public const val LINE_BACKGROUND_SHADER_START: Float = 0.5f

    /**
     * The default value for alpha on the end of line’s background gradient.
     */
    public const val LINE_BACKGROUND_SHADER_END: Float = 0f
}
