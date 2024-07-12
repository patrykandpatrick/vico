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

package com.patrykandpatrick.vico.core.common

import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.TextComponent

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
   * @param context the [MeasureContext] used to measure the height.
   * @param maxWidth the maximum width.
   * @param iconPaddingDp the padding between the icon and the label.
   * @param iconSizeDp the size of the icon.
   */
  public fun getLabelHeight(
    context: MeasureContext,
    maxWidth: Float,
    iconPaddingDp: Float,
    iconSizeDp: Float,
  ): Float =
    with(context) {
      labelComponent.getHeight(
        context = context,
        text = label,
        maxWidth = (maxWidth - iconSizeDp.pixels - iconPaddingDp.pixels).toInt(),
      )
    }

  /**
   * Measures the width of the label.
   *
   * @param context the [MeasureContext] used to measure the height.
   * @param maxWidth the maximum width.
   * @param iconPaddingDp the padding between the icon and the label.
   * @param iconSizeDp the size of the icon.
   */
  public fun getLabelWidth(
    context: MeasureContext,
    maxWidth: Float,
    iconPaddingDp: Float,
    iconSizeDp: Float,
  ): Float =
    with(context) {
      labelComponent.getWidth(
        context = context,
        text = label,
        maxWidth = (maxWidth - iconSizeDp.pixels - iconPaddingDp.pixels).toInt(),
      )
    }

  /**
   * Measures the width of this [LegendItem].
   *
   * @param context the [MeasureContext] used to measure the height.
   * @param maxWidth the maximum width.
   * @param iconPaddingDp the padding between the icon and the label.
   * @param iconSizeDp the size of the icon.
   */
  public fun getWidth(
    context: MeasureContext,
    maxWidth: Float,
    iconPaddingDp: Float,
    iconSizeDp: Float,
  ): Float =
    with(context) {
      getLabelWidth(context, maxWidth, iconPaddingDp, iconSizeDp) +
        (iconSizeDp + iconPaddingDp).pixels
    }
}
