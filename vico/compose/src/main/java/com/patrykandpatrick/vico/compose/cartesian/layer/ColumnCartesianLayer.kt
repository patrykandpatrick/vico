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
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer.MergeMode
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.DefaultDrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.data.DrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.Shape

/** Creates and remembers a [ColumnCartesianLayer]. */
@Composable
public fun rememberColumnCartesianLayer(
  columnProvider: ColumnCartesianLayer.ColumnProvider =
    ColumnCartesianLayer.ColumnProvider.series(
      vicoTheme.columnCartesianLayerColors.map { color ->
        rememberLineComponent(
          color,
          Defaults.COLUMN_WIDTH.dp,
          Shape.rounded(Defaults.COLUMN_ROUNDNESS_PERCENT),
        )
      }
    ),
  columnCollectionSpacing: Dp = Defaults.COLUMN_COLLECTION_SPACING.dp,
  mergeMode: (ExtraStore) -> MergeMode = { MergeMode.grouped() },
  verticalAxisPosition: Axis.Position.Vertical? = null,
  dataLabel: TextComponent? = null,
  dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
  dataLabelValueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
  dataLabelRotationDegrees: Float = 0f,
  axisValueOverrider: AxisValueOverrider = remember { AxisValueOverrider.auto() },
  drawingModelInterpolator:
    DrawingModelInterpolator<
      ColumnCartesianLayerDrawingModel.ColumnInfo,
      ColumnCartesianLayerDrawingModel,
    > =
    remember {
      DefaultDrawingModelInterpolator()
    },
): ColumnCartesianLayer =
  remember { ColumnCartesianLayer(columnProvider) }
    .apply {
      this.columnProvider = columnProvider
      this.columnCollectionSpacingDp = columnCollectionSpacing.value
      this.mergeMode = mergeMode
      this.dataLabel = dataLabel
      this.dataLabelVerticalPosition = dataLabelVerticalPosition
      this.dataLabelValueFormatter = dataLabelValueFormatter
      this.dataLabelRotationDegrees = dataLabelRotationDegrees
      this.axisValueOverrider = axisValueOverrider
      this.verticalAxisPosition = verticalAxisPosition
      this.drawingModelInterpolator = drawingModelInterpolator
    }

/** Creates a [MergeMode.Grouped] instance. */
public fun MergeMode.Companion.grouped(
  columnSpacing: Dp = Defaults.GROUPED_COLUMN_SPACING.dp
): MergeMode.Grouped = MergeMode.Grouped(columnSpacing.value)

/** Returns a [MergeMode.Stacked] instance. */
public fun MergeMode.Companion.stacked(): MergeMode.Stacked = MergeMode.Stacked
