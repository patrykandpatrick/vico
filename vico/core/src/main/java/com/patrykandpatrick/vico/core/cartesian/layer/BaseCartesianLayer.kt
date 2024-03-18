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

package com.patrykandpatrick.vico.core.cartesian.layer

import android.graphics.RectF
import com.patrykandpatrick.vico.core.cartesian.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.draw.CartesianChartDrawContext
import com.patrykandpatrick.vico.core.cartesian.insets.Insets
import com.patrykandpatrick.vico.core.cartesian.model.CartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.values.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.values.ChartValues
import com.patrykandpatrick.vico.core.common.dimension.BoundsAware
import com.patrykandpatrick.vico.core.common.extension.half
import com.patrykandpatrick.vico.core.common.extension.inClip

/**
 * A base [CartesianLayer] implementation.
 */
public abstract class BaseCartesianLayer<T : CartesianLayerModel> : CartesianLayer<T>, BoundsAware {
    private val insets: Insets = Insets()

    override val bounds: RectF = RectF()

    /** Overrides the _x_ and _y_ ranges. */
    public var axisValueOverrider: AxisValueOverrider = AxisValueOverrider.auto()

    protected abstract fun drawInternal(
        context: CartesianChartDrawContext,
        model: T,
    )

    protected fun MutableHorizontalDimensions.ensureSegmentedValues(
        xSpacing: Float,
        chartValues: ChartValues,
    ) {
        ensureValuesAtLeast(
            xSpacing = xSpacing,
            scalableStartPadding = xSpacing.half,
            scalableEndPadding = ((-chartValues.xLength / chartValues.xStep - 0.5f) % 1f + 1f) % 1f * xSpacing,
        )
    }

    override fun draw(
        context: CartesianChartDrawContext,
        model: T,
    ) {
        with(context) {
            insets.clear()
            getInsets(this, insets, horizontalDimensions)
            canvas.inClip(
                left = bounds.left - insets.getLeft(isLtr),
                top = bounds.top - insets.top,
                right = bounds.right + insets.getRight(isLtr),
                bottom = bounds.bottom + insets.bottom,
            ) {
                drawInternal(context, model)
            }
        }
    }
}
