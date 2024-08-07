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
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.DrawingContext
import com.patrykandpatrick.vico.core.common.HorizontalLegend
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.MeasuringContext
import com.patrykandpatrick.vico.core.common.VerticalLegend
import com.patrykandpatrick.vico.core.common.data.ExtraStore

/** Creates and remembers a [VerticalLegend]. */
@Composable
public fun <M : MeasuringContext, D : DrawingContext> rememberVerticalLegend(
  items: AdditionScope<LegendItem>.(ExtraStore) -> Unit,
  iconSize: Dp,
  iconPadding: Dp,
  spacing: Dp = 0.dp,
  padding: Dimensions = Dimensions.Empty,
): VerticalLegend<M, D> =
  remember(items, iconSize, iconPadding, spacing, padding) {
    VerticalLegend(
      items = items,
      iconSizeDp = iconSize.value,
      iconPaddingDp = iconPadding.value,
      spacingDp = spacing.value,
      padding = padding,
    )
  }

/** Creates and remembers a [HorizontalLegend]. */
@Composable
public fun <M : MeasuringContext, D : DrawingContext> rememberHorizontalLegend(
  items: AdditionScope<LegendItem>.(ExtraStore) -> Unit,
  iconSize: Dp,
  iconPadding: Dp,
  lineSpacing: Dp = 0.dp,
  spacing: Dp = 0.dp,
  padding: Dimensions = Dimensions.Empty,
): HorizontalLegend<M, D> =
  remember(items, iconSize, iconPadding, lineSpacing, spacing, padding) {
    HorizontalLegend(
      items = items,
      iconSizeDp = iconSize.value,
      iconPaddingDp = iconPadding.value,
      lineSpacingDp = lineSpacing.value,
      spacingDp = spacing.value,
      padding = padding,
    )
  }
