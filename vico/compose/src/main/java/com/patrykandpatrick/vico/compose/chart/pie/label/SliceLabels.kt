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

package com.patrykandpatrick.vico.compose.chart.pie.label

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.component.rememberTextComponent
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.pie.InsideSliceLabel
import com.patrykandpatrick.vico.core.pie.OutsideSliceLabel
import com.patrykandpatrick.vico.core.pie.SliceLabel

/**
 * The factory function for [InsideSliceLabel].
 *
 * @param textComponent the [TextComponent] to use for the label.
 *
 * @see InsideSliceLabel
 */
@Composable
public fun SliceLabel.Companion.rememberInsideLabel(
    textComponent: TextComponent = rememberTextComponent(),
): InsideSliceLabel =
    remember {
        InsideSliceLabel(textComponent)
    }.apply {
        this.textComponent = textComponent
    }

/**
 * The factory function for [OutsideSliceLabel].
 *
 * @param textComponent the [TextComponent] to use for the label.
 * @param lineColor the color of the line connecting the label to the slice.
 * @param lineWidth the width of the line connecting the label to the slice.
 * @param angledSegmentLength the length of the angled segment of the line connecting the label to the slice.
 * @param horizontalSegmentLength the length of the horizontal segment of the line connecting the label to the slice.
 * @param maxWidthToBoundsRatio the maximum width of the label as a ratio of the bounds of the slice.
 *
 * @see OutsideSliceLabel
 */
@Composable
public fun SliceLabel.Companion.rememberOutsideLabel(
    textComponent: TextComponent = rememberTextComponent(),
    lineColor: Color = Color.Black,
    lineWidth: Dp = DefaultDimens.SLICE_LINE_WIDTH.dp,
    angledSegmentLength: Dp = DefaultDimens.SLICE_ANGLED_SEGMENT_LENGTH.dp,
    horizontalSegmentLength: Dp = DefaultDimens.SLICE_HORIZONTAL_SEGMENT_LENGTH.dp,
    maxWidthToBoundsRatio: Float = DefaultDimens.SLICE_OUTSIDE_LABEL_MAX_WIDTH_TO_BOUNDS_RATIO,
): OutsideSliceLabel =
    remember {
        OutsideSliceLabel(
            textComponent = textComponent,
            lineColor = lineColor.toArgb(),
            lineWidthDp = lineWidth.value,
            angledSegmentLengthDp = angledSegmentLength.value,
            horizontalSegmentLengthDp = horizontalSegmentLength.value,
            maxWidthToBoundsRatio = maxWidthToBoundsRatio,
        )
    }.apply {
        this.textComponent = textComponent
        this.lineColor = lineColor.toArgb()
        this.lineWidthDp = lineWidth.value
        this.angledSegmentLengthDp = angledSegmentLength.value
        this.horizontalSegmentLengthDp = horizontalSegmentLength.value
        this.maxWidthToBoundsRatio = maxWidthToBoundsRatio
    }
