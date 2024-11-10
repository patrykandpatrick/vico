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
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer.MergeMode
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.ValueWrapper
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.getValue
import com.patrykandpatrick.vico.core.common.setValue
import com.patrykandpatrick.vico.core.common.shape.CorneredShape

/** Creates and remembers a [ColumnCartesianLayer]. */
@Composable
public fun rememberColumnCartesianLayer(
  columnProvider: ColumnCartesianLayer.ColumnProvider =
    ColumnCartesianLayer.ColumnProvider.series(
      vicoTheme.columnCartesianLayerColors.map { color ->
        rememberLineComponent(
          fill(color),
          Defaults.COLUMN_WIDTH.dp,
          CorneredShape.rounded(Defaults.COLUMN_ROUNDNESS_PERCENT),
        )
      }
    ),
  columnCollectionSpacing: Dp = Defaults.COLUMN_COLLECTION_SPACING.dp,
  mergeMode: (ExtraStore) -> MergeMode = { MergeMode.grouped() },
  dataLabel: TextComponent? = null,
  dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
  dataLabelValueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
  dataLabelRotationDegrees: Float = 0f,
  rangeProvider: CartesianLayerRangeProvider = remember { CartesianLayerRangeProvider.auto() },
  verticalAxisPosition: Axis.Position.Vertical? = null,
  drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<
      ColumnCartesianLayerDrawingModel.ColumnInfo,
      ColumnCartesianLayerDrawingModel,
    > =
    remember {
      CartesianLayerDrawingModelInterpolator.default()
    },
): ColumnCartesianLayer {
  var columnCartesianLayerWrapper by remember { ValueWrapper<ColumnCartesianLayer?>(null) }
  return remember(
    columnProvider,
    columnCollectionSpacing,
    mergeMode,
    dataLabel,
    dataLabelVerticalPosition,
    dataLabelValueFormatter,
    dataLabelRotationDegrees,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
  ) {
    val columnCartesianLayer =
      columnCartesianLayerWrapper?.copy(
        columnProvider,
        columnCollectionSpacing.value,
        mergeMode,
        dataLabel,
        dataLabelVerticalPosition,
        dataLabelValueFormatter,
        dataLabelRotationDegrees,
        rangeProvider,
        verticalAxisPosition,
        drawingModelInterpolator,
      )
        ?: ColumnCartesianLayer(
          columnProvider,
          columnCollectionSpacing.value,
          mergeMode,
          dataLabel,
          dataLabelVerticalPosition,
          dataLabelValueFormatter,
          dataLabelRotationDegrees,
          rangeProvider,
          verticalAxisPosition,
          drawingModelInterpolator,
        )
    columnCartesianLayerWrapper = columnCartesianLayer
    columnCartesianLayer
  }
}

/** Creates a [MergeMode.Grouped] instance. */
public fun MergeMode.Companion.grouped(
  columnSpacing: Dp = Defaults.GROUPED_COLUMN_SPACING.dp
): MergeMode.Grouped = MergeMode.Grouped(columnSpacing.value)

/** Returns a [MergeMode.Stacked] instance. */
public fun MergeMode.Companion.stacked(): MergeMode.Stacked = MergeMode.Stacked
