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

package com.patrykandpatrick.vico.compose.chart.layer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.theme.vicoTheme
import com.patrykandpatrick.vico.core.Defaults
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.layer.CartesianLayer
import com.patrykandpatrick.vico.core.chart.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.chart.layer.ColumnCartesianLayer.MergeMode
import com.patrykandpatrick.vico.core.chart.values.AxisValueOverrider
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.formatter.DecimalFormatValueFormatter
import com.patrykandpatrick.vico.core.formatter.ValueFormatter
import com.patrykandpatrick.vico.core.model.ExtraStore
import com.patrykandpatrick.vico.core.model.drawing.ColumnCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.model.drawing.DefaultDrawingModelInterpolator
import com.patrykandpatrick.vico.core.model.drawing.DrawingModelInterpolator

/**
 * Creates a [ColumnCartesianLayer].
 *
 * @param columns the [LineComponent] instances to use for columns. This list is iterated through as many times
 * as necessary for each column collection. If the list contains a single element, all columns have the same appearance.
 * @param spacing the distance between neighboring column collections.
 * @param innerSpacing the distance between neighboring grouped columns.
 * @param mergeMode defines how columns should be drawn in column collections.
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the top of their
 * respective columns.
 * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels (in degrees).
 * @param axisValueOverrider overrides the _x_ and _y_ ranges.
 * @param verticalAxisPosition the position of the [VerticalAxis] with which the [ColumnCartesianLayer] should be
 * associated. Use this for independent [CartesianLayer] scaling.
 * @param drawingModelInterpolator interpolates the [ColumnCartesianLayer]’s [ColumnCartesianLayerDrawingModel]s.
 */
@Composable
public fun rememberColumnCartesianLayer(
    columns: List<LineComponent> =
        vicoTheme.cartesianLayerColors.map { color ->
            rememberLineComponent(color = color, shape = Shapes.roundedCornerShape(Defaults.COLUMN_ROUNDNESS_PERCENT))
        },
    spacing: Dp = Defaults.COLUMN_OUTSIDE_SPACING.dp,
    innerSpacing: Dp = Defaults.COLUMN_INSIDE_SPACING.dp,
    mergeMode: (ExtraStore) -> MergeMode = { MergeMode.Grouped },
    verticalAxisPosition: AxisPosition.Vertical? = null,
    dataLabel: TextComponent? = null,
    dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
    dataLabelValueFormatter: ValueFormatter = remember { DecimalFormatValueFormatter() },
    dataLabelRotationDegrees: Float = 0f,
    axisValueOverrider: AxisValueOverrider = remember { AxisValueOverrider.auto() },
    drawingModelInterpolator:
        DrawingModelInterpolator<ColumnCartesianLayerDrawingModel.ColumnInfo, ColumnCartesianLayerDrawingModel> =
        remember { DefaultDrawingModelInterpolator() },
): ColumnCartesianLayer =
    remember { ColumnCartesianLayer() }.apply {
        this.columns = columns
        this.spacingDp = spacing.value
        this.innerSpacingDp = innerSpacing.value
        this.mergeMode = mergeMode
        this.dataLabel = dataLabel
        this.dataLabelVerticalPosition = dataLabelVerticalPosition
        this.dataLabelValueFormatter = dataLabelValueFormatter
        this.dataLabelRotationDegrees = dataLabelRotationDegrees
        this.axisValueOverrider = axisValueOverrider
        this.verticalAxisPosition = verticalAxisPosition
        this.drawingModelInterpolator = drawingModelInterpolator
    }
