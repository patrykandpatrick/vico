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

import androidx.compose.ui.unit.Dp
import com.patrykandpatrick.vico.multiplatform.common.component.Component
import com.patrykandpatrick.vico.multiplatform.common.component.TextComponent

/**
 * Defines the appearance of an item of a [Legend].
 *
 * @param icon used as the icon.
 * @param labelComponent the label [TextComponent].
 * @param label the label text.
 */
public open class LegendItem(
  public open val icon: Component,
  public open val labelComponent: TextComponent,
  public open val label: CharSequence,
) {
  /**
   * Measures the height of the label.
   *
   * @param iconSize the [LegendItem.icon] size.
   * @param iconLabelSpacing the spacing between [LegendItem.icon] and [LegendItem.labelComponent].
   * @param maxWidth the maximum [LegendItem] width.
   */
  public fun getLabelHeight(
    context: MeasuringContext,
    iconSize: Dp,
    iconLabelSpacing: Dp,
    maxWidth: Float,
  ): Float =
    labelComponent.getHeight(
      context = context,
      text = label,
      maxWidth = (maxWidth - context.run { iconSize.pixels + iconLabelSpacing.pixels }).toInt(),
    )

  /**
   * Measures the width of the label.
   *
   * @param iconSize the [LegendItem.icon] size.
   * @param iconLabelSpacing the spacing between [LegendItem.icon] and [LegendItem.labelComponent].
   * @param maxWidth the maximum [LegendItem] width.
   */
  public fun getLabelWidth(
    context: MeasuringContext,
    iconSize: Dp,
    iconLabelSpacing: Dp,
    maxWidth: Float,
  ): Float =
    labelComponent.getWidth(
      context = context,
      text = label,
      maxWidth = (maxWidth - context.run { iconSize.pixels + iconLabelSpacing.pixels }).toInt(),
    )

  /**
   * Measures the width of this [LegendItem].
   *
   * @param iconSize the [LegendItem.icon] size.
   * @param iconLabelSpacing the spacing between [LegendItem.icon] and [LegendItem.labelComponent].
   * @param maxWidth the maximum [LegendItem] width.
   */
  public fun getWidth(
    context: MeasuringContext,
    iconSize: Dp,
    iconLabelSpacing: Dp,
    maxWidth: Float,
  ): Float =
    getLabelWidth(context, iconSize, iconLabelSpacing, maxWidth) +
      context.run { (iconSize + iconLabelSpacing).pixels }
}
