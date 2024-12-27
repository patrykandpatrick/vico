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

package com.patrykandpatrick.vico.compose.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.common.AdditionScope
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.DrawingContext
import com.patrykandpatrick.vico.core.common.HorizontalLegend
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.MeasuringContext
import com.patrykandpatrick.vico.core.common.VerticalLegend
import com.patrykandpatrick.vico.core.common.data.ExtraStore

/** Creates and remembers a [VerticalLegend]. */
@Composable
public fun <M : MeasuringContext, D : DrawingContext> rememberVerticalLegend(
  items: AdditionScope<LegendItem>.(ExtraStore) -> Unit,
  iconSize: Dp = Defaults.LEGEND_ICON_SIZE.dp,
  iconLabelSpacing: Dp = Defaults.LEGEND_ICON_LABEL_SPACING.dp,
  rowSpacing: Dp = Defaults.LEGEND_ROW_SPACING.dp,
  padding: Insets = Insets.Zero,
): VerticalLegend<M, D> =
  remember(items, iconSize, iconLabelSpacing, rowSpacing, padding) {
    VerticalLegend(items, iconSize.value, iconLabelSpacing.value, rowSpacing.value, padding)
  }

/** Creates and remembers a [HorizontalLegend]. */
@Composable
public fun <M : MeasuringContext, D : DrawingContext> rememberHorizontalLegend(
  items: AdditionScope<LegendItem>.(ExtraStore) -> Unit,
  iconSize: Dp = Defaults.LEGEND_ICON_SIZE.dp,
  iconLabelSpacing: Dp = Defaults.LEGEND_ICON_LABEL_SPACING.dp,
  rowSpacing: Dp = Defaults.LEGEND_ROW_SPACING.dp,
  columnSpacing: Dp = Defaults.LEGEND_COLUMN_SPACING.dp,
  padding: Insets = Insets.Zero,
): HorizontalLegend<M, D> =
  remember(items, iconSize, iconLabelSpacing, rowSpacing, columnSpacing, padding) {
    HorizontalLegend(
      items,
      iconSize.value,
      iconLabelSpacing.value,
      rowSpacing.value,
      columnSpacing.value,
      padding,
    )
  }
