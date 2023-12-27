/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.chart.pie.label

import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.pie.PieChart
import com.patrykandpatrick.vico.core.chart.pie.slice.Slice
import com.patrykandpatrick.vico.core.context.DrawContext

/**
 * A base class for a label for a [Slice].
 */
public abstract class SliceLabel {
    /**
     * Draws the label for the [Slice].
     *
     * @param context the [DrawContext] used to draw the label.
     * @param oval the oval used to draw the [PieChart].
     * @param holeRadius the radius of the hole in the [PieChart].
     * @param angle the angle of the [Slice].
     * @param slicePath the [Path] of the [Slice].
     * @param label the label to draw.
     */
    public abstract fun drawLabel(
        context: DrawContext,
        oval: RectF,
        holeRadius: Float,
        angle: Float,
        slicePath: Path,
        label: CharSequence,
        sliceOpacity: Float,
        labelOpacity: Float,
    )

    /**
     * Fills the [outInsets] for the label. The insets have an effect on the final size of the [PieChart].
     *
     * @param context the [DrawContext] used to draw the label.
     * @param contentBounds the bounds of the content of the [PieChart].
     * @param oval the oval used to draw the [PieChart].
     * @param angle the angle of the [Slice].
     * @param label the label to draw.
     * @param outInsets the [Insets] to fill with the insets.
     */
    public open fun getInsets(
        context: DrawContext,
        contentBounds: RectF,
        oval: RectF,
        angle: Float,
        label: CharSequence,
        outInsets: Insets,
    ): Unit = Unit

    public companion object
}
