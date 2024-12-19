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

package com.patrykandpatrick.vico.core.common

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
  public const val GROUPED_COLUMN_SPACING: Float = 8f
  public const val COLUMN_COLLECTION_SPACING: Float = 32f
  public const val COLUMN_ROUNDNESS_PERCENT: Int = 40
  public const val CANDLE_BODY_WIDTH_DP: Float = 8f
  public const val MIN_CANDLE_BODY_HEIGHT_DP: Float = 1f
  public const val WICK_DEFAULT_WIDTH_DP: Float = 1f
  public const val HOLLOW_CANDLE_STROKE_THICKNESS_DP: Float = 1f
  public const val CANDLE_SPACING_DP: Float = 4f
  public const val LINE_CURVATURE: Float = 0.5f
  public const val LINE_PATTERN_DASHED_LENGTH: Float = 4f
  public const val LINE_PATTERN_DASHED_GAP: Float = 8f
  public const val DASH_LENGTH: Float = 4f
  public const val DASH_GAP: Float = 2f
  public const val FADING_EDGE_VISIBILITY_THRESHOLD_DP: Float = 16f
  public const val FADING_EDGE_WIDTH_DP: Float = 32f
  public const val TEXT_COMPONENT_LINE_COUNT: Int = 1
  public const val LINE_COMPONENT_THICKNESS_DP: Float = 1f
  public const val LINE_SPEC_THICKNESS_DP: Float = 2f
  public const val MARKER_INDICATOR_SIZE: Float = 16f
  public const val MARKER_HORIZONTAL_PADDING: Float = 8f
  public const val MARKER_VERTICAL_PADDING: Float = 4f
  public const val MARKER_TICK_SIZE: Float = 6f
  public const val MAX_ZOOM: Float = 10f
  public const val POINT_SIZE: Float = 16f
  public const val POINT_SPACING: Float = 32f
  public const val TEXT_COMPONENT_TEXT_SIZE: Float = 12f
  public const val THRESHOLD_LINE_THICKNESS: Float = 2f
  public const val SHADOW_COLOR: Int = 0x8A000000.toInt()
  public const val CHART_HEIGHT: Float = 200f
  public const val LEGEND_COLUMN_SPACING: Float = 16f
  public const val LEGEND_ICON_LABEL_SPACING: Float = 8f
  public const val LEGEND_ICON_SIZE: Float = 8f
  public const val LEGEND_ROW_SPACING: Float = 8f
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DefaultColors(
  public val bullishCandleColor: Long,
  public val neutralCandleColor: Long,
  public val bearishCandleColor: Long,
  public val cartesianLayerColors: List<Long>,
  public val lineColor: Long,
  public val textColor: Long,
) {
  public companion object {
    public val Light: DefaultColors =
      DefaultColors(
        bullishCandleColor = 0xff02c898,
        neutralCandleColor = 0xff212121,
        bearishCandleColor = 0xffea284b,
        cartesianLayerColors = listOf(0xff787878, 0xff5a5a5a, 0xff383838),
        lineColor = 0x47000000,
        textColor = 0xde000000,
      )

    public val Dark: DefaultColors =
      DefaultColors(
        bullishCandleColor = 0xff02c898,
        neutralCandleColor = 0xff8a8a8a,
        bearishCandleColor = 0xffea284b,
        cartesianLayerColors = listOf(0xffcacaca, 0xffa8a8a8, 0xff888888),
        lineColor = 0xff555555,
        textColor = 0xffffffff,
      )
  }
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public object DefaultAlpha {
  public const val LINE_BACKGROUND_SHADER_START: Float = 0.5f
  public const val LINE_BACKGROUND_SHADER_END: Float = 0f
}
