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

package com.patrykandpatrick.vico.compose.cartesian.layer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.formatter.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.formatter.DecimalFormatValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer.MergeMode
import com.patrykandpatrick.vico.core.cartesian.model.ColumnCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.cartesian.values.AxisValueOverrider
import com.patrykandpatrick.vico.core.common.DefaultDrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.DrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.ExtraStore
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.position.VerticalPosition
import com.patrykandpatrick.vico.core.common.shape.Shapes

/** Creates and remembers a [ColumnCartesianLayer]. */
@Composable
public fun rememberColumnCartesianLayer(
    columnProvider: ColumnCartesianLayer.ColumnProvider =
        ColumnCartesianLayer.ColumnProvider.series(
            vicoTheme.cartesianLayerColors.map { color ->
                rememberLineComponent(
                    color,
                    Defaults.COLUMN_WIDTH.dp,
                    Shapes.roundedCornerShape(Defaults.COLUMN_ROUNDNESS_PERCENT),
                )
            },
        ),
    spacing: Dp = Defaults.COLUMN_OUTSIDE_SPACING.dp,
    innerSpacing: Dp = Defaults.COLUMN_INSIDE_SPACING.dp,
    mergeMode: (ExtraStore) -> MergeMode = { MergeMode.Grouped },
    verticalAxisPosition: AxisPosition.Vertical? = null,
    dataLabel: TextComponent? = null,
    dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
    dataLabelValueFormatter: CartesianValueFormatter = remember { DecimalFormatValueFormatter() },
    dataLabelRotationDegrees: Float = 0f,
    axisValueOverrider: AxisValueOverrider = remember { AxisValueOverrider.auto() },
    drawingModelInterpolator:
        DrawingModelInterpolator<ColumnCartesianLayerDrawingModel.ColumnInfo, ColumnCartesianLayerDrawingModel> =
        remember { DefaultDrawingModelInterpolator() },
): ColumnCartesianLayer =
    remember { ColumnCartesianLayer(columnProvider) }.apply {
        this.columnProvider = columnProvider
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

/**
 * Creates and remembers a [ColumnCartesianLayer] with the provided column [LineComponent]s ([columns]). One
 * [LineComponent] is used per series. The [LineComponent]s and series are associated by index. If there are more series
 * than [LineComponent]s, [columns] is iterated multiple times.
 */
@Composable
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated("Replace `columns = ...` with `columnProvider = ColumnCartesianLayer.ColumnProvider.series(...)`.")
public fun rememberColumnCartesianLayer(
    columns: List<LineComponent>,
    spacing: Dp = Defaults.COLUMN_OUTSIDE_SPACING.dp,
    innerSpacing: Dp = Defaults.COLUMN_INSIDE_SPACING.dp,
    mergeMode: (ExtraStore) -> MergeMode = { MergeMode.Grouped },
    verticalAxisPosition: AxisPosition.Vertical? = null,
    dataLabel: TextComponent? = null,
    dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
    dataLabelValueFormatter: CartesianValueFormatter = remember { DecimalFormatValueFormatter() },
    dataLabelRotationDegrees: Float = 0f,
    axisValueOverrider: AxisValueOverrider = remember { AxisValueOverrider.auto() },
    drawingModelInterpolator:
        DrawingModelInterpolator<ColumnCartesianLayerDrawingModel.ColumnInfo, ColumnCartesianLayerDrawingModel> =
        remember { DefaultDrawingModelInterpolator() },
): ColumnCartesianLayer =
    rememberColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(columns),
        spacing,
        innerSpacing,
        mergeMode,
        verticalAxisPosition,
        dataLabel,
        dataLabelVerticalPosition,
        dataLabelValueFormatter,
        dataLabelRotationDegrees,
        axisValueOverrider,
        drawingModelInterpolator,
    )
