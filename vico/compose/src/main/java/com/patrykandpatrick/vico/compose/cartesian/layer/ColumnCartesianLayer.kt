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
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer.MergeMode
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.ValueWrapper
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.getValue
import com.patrykandpatrick.vico.core.common.setValue

/** Creates and remembers a [ColumnCartesianLayer]. */
@Composable
public fun rememberColumnCartesianLayer(
  columnProvider: ColumnCartesianLayer.ColumnProvider =
    ColumnCartesianLayer.ColumnProvider.series(
      vicoTheme.columnCartesianLayerColors.map { color ->
        rememberLineComponent(fill(color), Defaults.COLUMN_WIDTH.dp)
      }
    ),
  columnCollectionSpacing: Dp = Defaults.COLUMN_COLLECTION_SPACING.dp,
  mergeMode: (ExtraStore) -> MergeMode = { MergeMode.grouped() },
  dataLabel: TextComponent? = null,
  dataLabelPosition: Position.Vertical = Position.Vertical.Top,
  dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.Default,
  dataLabelRotationDegrees: Float = 0f,
  rangeProvider: CartesianLayerRangeProvider = remember { CartesianLayerRangeProvider.auto() },
  verticalAxisPosition: Axis.Position.Vertical? = null,
  drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<
      ColumnCartesianLayerDrawingModel.Entry,
      ColumnCartesianLayerDrawingModel,
    > =
    remember {
      CartesianLayerDrawingModelInterpolator.default()
    },
  onColumnClick: ((ColumnCartesianLayerModel.Entry, Int) -> Unit)? = null,
): ColumnCartesianLayer {
  var columnCartesianLayerWrapper by remember { ValueWrapper<ColumnCartesianLayer?>(null) }
  return remember(
    columnProvider,
    columnCollectionSpacing,
    mergeMode,
    dataLabel,
    dataLabelPosition,
    dataLabelValueFormatter,
    dataLabelRotationDegrees,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
    onColumnClick,
  ) {
    val columnCartesianLayer =
      columnCartesianLayerWrapper?.copy(
        columnProvider,
        columnCollectionSpacing.value,
        mergeMode,
        dataLabel,
        dataLabelPosition,
        dataLabelValueFormatter,
        dataLabelRotationDegrees,
        rangeProvider,
        verticalAxisPosition,
        drawingModelInterpolator,
        onColumnClick,
      )
        ?: ColumnCartesianLayer(
          columnProvider,
          columnCollectionSpacing.value,
          mergeMode,
          dataLabel,
          dataLabelPosition,
          dataLabelValueFormatter,
          dataLabelRotationDegrees,
          rangeProvider,
          verticalAxisPosition,
          drawingModelInterpolator,
          onColumnClick,
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
