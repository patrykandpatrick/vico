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

package com.patrykandpatrick.vico.compose.common.legend

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.legend.HorizontalLegend
import com.patrykandpatrick.vico.core.common.legend.Legend
import com.patrykandpatrick.vico.core.common.legend.LegendItem
import com.patrykandpatrick.vico.core.common.legend.VerticalLegend

/**
 * Creates a [VerticalLegend].
 *
 * @param items a [Collection] of [LegendItem]s to be displayed by this [VerticalLegend].
 * @param iconSize defines the size of all [LegendItem.icon]s.
 * @param iconPadding defines the padding between each [LegendItem.icon] and its corresponding [LegendItem.label].
 * @param spacing defines the vertical spacing between each [LegendItem].
 * @param padding defines the padding of the content.
 */
@Composable
public fun rememberVerticalLegend(
    items: Collection<LegendItem>,
    iconSize: Dp,
    iconPadding: Dp,
    spacing: Dp = 0.dp,
    padding: Dimensions = Dimensions.Empty,
): VerticalLegend =
    remember(items, iconSize, iconPadding, spacing, padding) {
        VerticalLegend(
            items = items,
            iconSizeDp = iconSize.value,
            iconPaddingDp = iconPadding.value,
            spacingDp = spacing.value,
            padding = padding,
        )
    }

/**
 * Defines the appearance of an item of a [Legend].
 *
 * @param icon the [Component] used as the item’s icon.
 * @param label the [TextComponent] used for the label.
 * @param labelText the text content of the label.
 */
@Composable
public fun rememberLegendItem(
    icon: Component,
    label: TextComponent,
    labelText: CharSequence,
): LegendItem =
    remember(icon, label, labelText) {
        LegendItem(
            icon = icon,
            label = label,
            labelText = labelText,
        )
    }

/**
 * Creates a [HorizontalLegend].
 *
 * @param items a [Collection] of [LegendItem]s to be displayed by this [HorizontalLegend].
 * @param iconSize defines the size of all [LegendItem.icon]s.
 * @param iconPadding defines the padding between each [LegendItem.icon] and its corresponding [LegendItem.label].
 * @param lineSpacing defines the spacing between adjacent lines.
 * @param spacing defines the horizontal spacing between adjacent [LegendItem]s.
 * @param padding defines the padding of the content.
 */
@Composable
public fun rememberHorizontalLegend(
    items: Collection<LegendItem>,
    iconSize: Dp,
    iconPadding: Dp,
    lineSpacing: Dp = 0.dp,
    spacing: Dp = 0.dp,
    padding: Dimensions = Dimensions.Empty,
): HorizontalLegend =
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
