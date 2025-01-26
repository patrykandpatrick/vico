/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.multiplatform.common

internal object Animation {
  val range: ClosedFloatingPointRange<Float> = 0f..1f
  const val DIFF_DURATION: Int = 500
}

internal object Defaults {
  const val AXIS_LABEL_HORIZONTAL_PADDING: Int = 4
  const val AXIS_LABEL_MAX_LINES: Int = 1
  const val AXIS_LABEL_VERTICAL_PADDING: Int = 2
  const val AXIS_LABEL_SIZE: Int = 12
  const val AXIS_LABEL_HORIZONTAL_MARGIN: Int = 0
  const val AXIS_LABEL_VERTICAL_MARGIN: Int = 0
  const val AXIS_LABEL_ROTATION_DEGREES: Float = 0f
  const val AXIS_GUIDELINE_WIDTH: Float = 1f
  const val AXIS_LINE_WIDTH: Float = 1f
  const val AXIS_TICK_LENGTH: Float = 4f
  const val COLUMN_WIDTH: Float = 16f
  const val GROUPED_COLUMN_SPACING: Float = 8f
  const val COLUMN_COLLECTION_SPACING: Float = 16f
  const val CANDLE_BODY_WIDTH_DP: Float = 8f
  const val MIN_CANDLE_BODY_HEIGHT_DP: Float = 1f
  const val WICK_DEFAULT_WIDTH_DP: Float = 1f
  const val HOLLOW_CANDLE_STROKE_THICKNESS_DP: Float = 1f
  const val CANDLE_SPACING_DP: Float = 4f
  const val LINE_DASH_LENGTH: Float = 8f
  const val LINE_GAP_LENGTH: Float = 4f
  const val DASHED_SHAPE_DASH_LENGTH: Float = 4f
  const val DASHED_SHAPE_GAP_LENGTH: Float = 2f
  const val FADING_EDGE_VISIBILITY_THRESHOLD_DP: Float = 16f
  const val FADING_EDGE_WIDTH_DP: Float = 32f
  const val TEXT_COMPONENT_LINE_COUNT: Int = 1
  const val LINE_COMPONENT_THICKNESS_DP: Float = 1f
  const val LINE_SPEC_THICKNESS_DP: Float = 2f
  const val MARKER_INDICATOR_SIZE: Float = 16f
  const val MARKER_HORIZONTAL_PADDING: Float = 8f
  const val MARKER_VERTICAL_PADDING: Float = 4f
  const val MARKER_TICK_SIZE: Float = 6f
  const val MAX_ZOOM: Float = 10f
  const val POINT_SIZE: Float = 8f
  const val POINT_SPACING: Float = 32f
  const val TEXT_COMPONENT_TEXT_SIZE: Float = 12f
  const val THRESHOLD_LINE_THICKNESS: Float = 2f
  const val SHADOW_COLOR: Int = 0x8A000000.toInt()
  const val CHART_HEIGHT: Float = 200f
  const val LEGEND_COLUMN_SPACING: Float = 16f
  const val LEGEND_ICON_LABEL_SPACING: Float = 8f
  const val LEGEND_ICON_SIZE: Float = 8f
  const val LEGEND_ROW_SPACING: Float = 8f
}

internal class DefaultColors(
  val bullishCandleColor: Long,
  val neutralCandleColor: Long,
  val bearishCandleColor: Long,
  val cartesianLayerColors: List<Long>,
  val lineColor: Long,
  val textColor: Long,
) {
  companion object {
    val Light: DefaultColors =
      DefaultColors(
        bullishCandleColor = 0xff0ac285,
        neutralCandleColor = 0xff000000,
        bearishCandleColor = 0xffe8304f,
        cartesianLayerColors = listOf(0xff3287ff, 0xff0ac285, 0xffffab02),
        lineColor = 0xffbcbfc2,
        textColor = 0xff000000,
      )

    val Dark: DefaultColors =
      DefaultColors(
        bullishCandleColor = 0xff0ac285,
        neutralCandleColor = 0xffffffff,
        bearishCandleColor = 0xffe8304f,
        cartesianLayerColors = listOf(0xff3287ff, 0xff0ac285, 0xffffab02),
        lineColor = 0xff494c50,
        textColor = 0xffffffff,
      )
  }
}
