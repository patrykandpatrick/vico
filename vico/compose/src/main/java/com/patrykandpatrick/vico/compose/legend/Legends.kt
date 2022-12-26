/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.legend

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions
import com.patrykandpatrick.vico.core.legend.Legend
import com.patrykandpatrick.vico.core.legend.VerticalLegend
import com.patrykandpatrick.vico.core.legend.VerticalLegend.Item

/**
 * Creates a [VerticalLegend].
 *
 * @param items a [Collection] of [Item]s to be displayed by this [VerticalLegend].
 * @param iconSize defines the size of all [Item.icon]s.
 * @param iconPadding defines the padding between each [Item.icon] and its corresponding [Item.label].
 * @param spacing defines the vertical spacing between each [Item].
 * @param padding defines the padding of the content.
 */
@Composable
public fun verticalLegend(
    items: Collection<Item>,
    iconSize: Dp,
    iconPadding: Dp,
    spacing: Dp = 0.dp,
    padding: MutableDimensions = emptyDimensions(),
): VerticalLegend = remember(items, iconSize, iconPadding, spacing, padding) {
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
public fun verticalLegendItem(
    icon: Component,
    label: TextComponent,
    labelText: CharSequence,
): Item = remember(icon, label, labelText) {
    Item(
        icon = icon,
        label = label,
        labelText = labelText,
    )
}
