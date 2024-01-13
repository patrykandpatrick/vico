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

package com.patrykandpatrick.vico.views.chart.pie.label

import android.graphics.Color
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.chart.pie.label.InsideSliceLabel
import com.patrykandpatrick.vico.core.chart.pie.label.OutsideSliceLabel
import com.patrykandpatrick.vico.core.chart.pie.label.SliceLabel
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.textComponent

/**
 * The factory function for [InsideSliceLabel].
 *
 * @param textComponent the [TextComponent] to use for the label.
 *
 * @see InsideSliceLabel
 */
public fun SliceLabel.Companion.insideLabel(textComponent: TextComponent = textComponent()): SliceLabel =
    InsideSliceLabel(textComponent = textComponent)

/**
 * The factory function for [OutsideSliceLabel].
 *
 * @param textComponent the [TextComponent] to use for the label.
 * @param lineColor the color of the line connecting the label to the slice.
 * @param lineWidthDp the width of the line connecting the label to the slice (in dp).
 * @param angledSegmentLengthDp the length of the angled segment of the line connecting the label to the slice (in dp).
 * @param horizontalSegmentLengthDp the length of the horizontal segment of the line connecting the label to the slice
 * (in dp).
 * @param maxWidthToBoundsRatio the maximum width of the label as a ratio of the bounds of the slice.
 *
 * @see OutsideSliceLabel
 */
public fun SliceLabel.Companion.outsideLabel(
    textComponent: TextComponent = textComponent(),
    lineColor: Int = Color.BLACK,
    lineWidthDp: Float = 1f,
    angledSegmentLengthDp: Float = DefaultDimens.SLICE_ANGLED_SEGMENT_LENGTH,
    horizontalSegmentLengthDp: Float = DefaultDimens.SLICE_HORIZONTAL_SEGMENT_LENGTH,
    maxWidthToBoundsRatio: Float = DefaultDimens.SLICE_OUTSIDE_LABEL_MAX_WIDTH_TO_BOUNDS_RATIO,
): OutsideSliceLabel =
    OutsideSliceLabel(
        textComponent = textComponent,
        lineColor = lineColor,
        lineWidthDp = lineWidthDp,
        angledSegmentLengthDp = angledSegmentLengthDp,
        horizontalSegmentLengthDp = horizontalSegmentLengthDp,
        maxWidthToBoundsRatio = maxWidthToBoundsRatio,
    )
