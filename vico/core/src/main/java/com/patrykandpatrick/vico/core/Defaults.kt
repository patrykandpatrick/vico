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

package com.patrykandpatrick.vico.core

import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.text.TextComponent

/**
 * The default line count for [TextComponent].
 */
public const val DEF_LABEL_LINE_COUNT: Int = 1

/**
 * The default maximum label count for vertical axes.
 */
public const val DEF_LABEL_COUNT: Int = 100

/**
 * The default label spacing for vertical axes (in dp).
 */
public const val DEF_LABEL_SPACING: Float = 16f

/**
 * The default tick size for markers (in dp).
 */
public const val DEF_MARKER_TICK_SIZE: Float = 6f

/**
 * The default maximum zoom factor.
 */
public const val DEF_MAX_ZOOM: Float = 10f

/**
 * The default minimum zoom factor.
 */
public const val DEF_MIN_ZOOM: Float = 0.1f

/**
 * The default color for shadows.
 */
public const val DEF_SHADOW_COLOR: Int = 0x8A000000.toInt()

/**
 * The default size of the thread pools used for difference animations.
 */
public const val DEF_THREAD_POOL_SIZE: Int = 4

/**
 * The default scroll distance over which fading edges fade in and out (in dp).
 */
public const val FADING_EDGE_VISIBILITY_THRESHOLD_DP: Float = 16f

/**
 * The default width of fading edges.
 */
public const val FADING_EDGE_WIDTH_DP: Float = 32f

/**
 * Defaults for animations.
 */
public object Animation {
    /**
     * The [Float] range of values used in difference animations.
     */
    public val range: ClosedFloatingPointRange<Float> = 0f..1f

    /**
     * The default duration for difference animations.
     */
    public const val DIFF_DURATION: Int = 500

    /**
     * The default duration for animated scrolls (in milliseconds;
     * [com.patrykandpatrick.vico.views.chart.BaseChartView.animateScrollBy]).
     */
    public const val ANIMATED_SCROLL_DURATION: Int = 300
}

/**
 * The default chart dimensions.
 */
public object DefaultDimens {
    /**
     * The default horizontal padding for axis labels (in dp).
     */
    public const val AXIS_LABEL_HORIZONTAL_PADDING: Int = 4

    /**
     * The default maximum line count for axis labels.
     */
    public const val AXIS_LABEL_MAX_LINES: Int = 1

    /**
     * The default vertical padding for axis labels (in dp).
     */
    public const val AXIS_LABEL_VERTICAL_PADDING: Int = 2

    /**
     * The default text size for axis labels (in sp).
     */
    public const val AXIS_LABEL_SIZE: Int = 12

    /**
     * The default horizontal margin for axis labels (in dp).
     */
    public const val AXIS_LABEL_HORIZONTAL_MARGIN: Int = 0

    /**
     * The default vertical margin for axis labels (in dp).
     */
    public const val AXIS_LABEL_VERTICAL_MARGIN: Int = 0

    /**
     * The default rotation for axis labels (in degrees).
     */
    public const val AXIS_LABEL_ROTATION_DEGREES: Float = 0f

    /**
     * The default width for axis guidelines (in dp).
     */
    public const val AXIS_GUIDELINE_WIDTH: Float = 1f

    /**
     * The default width for axis lines (in dp).
     */
    public const val AXIS_LINE_WIDTH: Float = 1f

    /**
     * The default length for axis ticks (in dp).
     */
    public const val AXIS_TICK_LENGTH: Float = 4f

    /**
     * The default width for columns (in dp).
     */
    public const val COLUMN_WIDTH: Float = 8f

    /**
     * The default spacing (in dp) between the columns in a chart segment.
     */
    public const val COLUMN_INSIDE_SPACING: Float = 8f

    /**
     * The default spacing (in dp) between the left and right edges of a chart segment and the columns it contains.
     */
    public const val COLUMN_OUTSIDE_SPACING: Float = 32f

    /**
     * The default corner radius for columns (in percent).
     */
    public const val COLUMN_ROUNDNESS_PERCENT: Int = 40

    /**
     * The default cubic bezier strength for line charts.
     */
    public const val CUBIC_STRENGTH: Float = 1f

    /**
     * The default dash length for [DashedShape] (in dp).
     */
    public const val DASH_LENGTH: Float = 4f

    /**
     * The default dash gap for [DashedShape] (in dp).
     */
    public const val DASH_GAP: Float = 2f

    /**
     * The default line thickness for line charts (in dp).
     */
    public const val LINE_THICKNESS: Float = 2f

    /**
     * The default size for marker indicators (in dp).
     */
    public const val MARKER_INDICATOR_SIZE: Float = 36f

    /**
     * The default horizontal padding for markers (in dp).
     */
    public const val MARKER_HORIZONTAL_PADDING: Float = 8f

    /**
     * The default vertical padding for markers (in dp).
     */
    public const val MARKER_VERTICAL_PADDING: Float = 4f

    /**
     * The default size for line chart points (in dp).
     */
    public const val POINT_SIZE: Float = 16f

    /**
     * The default spacing for line chart points (in dp).
     */
    public const val POINT_SPACING: Float = 16f

    /**
     * The default text size for [TextComponent] (in sp).
     */
    public const val TEXT_COMPONENT_TEXT_SIZE: Float = 12f

    /**
     * The default thickness for threshold lines (in dp).
     */
    public const val THRESHOLD_LINE_THICKNESS: Float = 2f

    /**
     * The default height for charts (in dp).
     */
    public const val CHART_HEIGHT: Float = 200f
}

/**
 * The default chart colors.
 */
@Suppress("MagicNumber")
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
     * The color for columns whose index in a segment is 3k (k ∈ N)
     * and for lines whose index in the list of lines in a line chart is 3k (k ∈ N).
     */
    public val entity1Color: Long

    /**
     * The color for columns whose index in a segment is 1 + 3k (k ∈ N)
     * and for lines whose index in the list of lines in a line chart is 1 + 3k (k ∈ N).
     */
    public val entity2Color: Long

    /**
     * The color for columns whose index in a segment is 2 + 3k (k ∈ N)
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
