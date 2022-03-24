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

package pl.patrykgoworowski.vico.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader

internal data class ChartStyleOverrides(
    var axis: Axis = Axis(),
    var columnChart: ColumnChart = ColumnChart(),
    var lineChart: LineChart = LineChart(),
    var marker: Marker = Marker(),
) {
    internal data class Axis(
        var axisLabelBackground: ShapeComponent? = null,
        var axisLabelColor: Color? = null,
        var axisLabelTextSize: TextUnit? = null,
        var axisLabelLineCount: Int? = null,
        var axisLabelVerticalPadding: Dp? = null,
        var axisLabelHorizontalPadding: Dp? = null,
        var axisLabelVerticalMargin: Dp? = null,
        var axisLabelHorizontalMargin: Dp? = null,
        var axisLabelRotationDegrees: Float? = null,
        var axisGuidelineColor: Color? = null,
        var axisGuidelineWidth: Dp? = null,
        var axisGuidelineShape: Shape? = null,
        var axisLineColor: Color? = null,
        var axisLineWidth: Dp? = null,
        var axisLineShape: Shape? = null,
        var axisTickColor: Color? = null,
        var axisTickWidth: Dp? = null,
        var axisTickShape: Shape? = null,
        var axisTickLength: Dp? = null,
        var axisValueFormatter: AxisValueFormatter<AxisPosition>? = null,
    )

    internal data class ColumnChart(
        var columns: List<LineComponent>? = null,
        var outsideSpacing: Dp? = null,
        var innerSpacing: Dp? = null,
    )

    internal data class LineChart(
        var getPoint: Component? = null,
        var pointSize: Dp? = null,
        var spacing: Dp? = null,
        var lineWidth: Dp? = null,
        var lineColor: Color? = null,
        var lineBackgroundShader: DynamicShader? = null,
    )

    internal data class Marker(
        var indicatorSize: Dp? = null,
        var horizontalPadding: Dp? = null,
        var verticalPadding: Dp? = null,
    )
}
