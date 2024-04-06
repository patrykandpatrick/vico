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

package com.patrykandpatrick.vico.core.cartesian

import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.model.CartesianChartModel
import com.patrykandpatrick.vico.core.common.extension.orZero
import kotlin.math.max
import kotlin.math.min

/**
 * An implementation of [ChartValues] whose every property is mutable.
 */
public class MutableChartValues : ChartValues {
    private var _minX: Float? = null

    private var _maxX: Float? = null

    private var _xStep: Float? = null

    internal val yRanges: MutableMap<AxisPosition.Vertical?, MutableYRange> = mutableMapOf()

    override val minX: Float
        get() = _minX.orZero

    override val maxX: Float
        get() = _maxX.orZero

    override val xStep: Float
        get() = _xStep ?: 1f

    override fun getYRange(axisPosition: AxisPosition.Vertical?): ChartValues.YRange =
        yRanges[axisPosition] ?: yRanges.getValue(null)

    override var model: CartesianChartModel = CartesianChartModel.empty

    /**
     * Updates [MutableChartValues.xStep] and [MutableChartValues.model].
     */
    public fun update(
        xStep: Float? = null,
        model: CartesianChartModel,
    ) {
        _xStep = xStep
        this.model = model
    }

    /**
     * Tries to update the stored values. A minimum value can only be decreased. A maximum value can only be increased.
     */
    public fun tryUpdate(
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float,
        axisPosition: AxisPosition.Vertical?,
    ) {
        _minX = _minX?.coerceAtMost(minX) ?: minX
        _maxX = _maxX?.coerceAtLeast(maxX) ?: maxX
        yRanges[null]?.tryUpdate(minY, maxY) ?: run { yRanges[null] = MutableYRange(minY, maxY) }
        if (axisPosition != null) {
            yRanges[axisPosition]?.tryUpdate(minY, maxY) ?: run { yRanges[axisPosition] = MutableYRange(minY, maxY) }
        }
    }

    /**
     * Clears all values.
     */
    public fun reset() {
        _minX = null
        _maxX = null
        yRanges.clear()
        _xStep = null
        model = CartesianChartModel.empty
    }

    /**
     * A mutable implementation of [ChartValues.YRange].
     */
    public class MutableYRange(override var minY: Float, override var maxY: Float) : ChartValues.YRange {
        override val length: Float get() = maxY - minY

        /**
         * Tries to update [MutableYRange.minY] and [MutableYRange.maxY]. [MutableYRange.minY] can only be decreased.
         * [MutableYRange.maxY] can only be increased.
         */
        public fun tryUpdate(
            minY: Float,
            maxY: Float,
        ) {
            this.minY = min(this.minY, minY)
            this.maxY = max(this.maxY, maxY)
        }
    }
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun MutableChartValues.toImmutable(): ChartValues =
    object : ChartValues {
        private val yRanges = this@toImmutable.yRanges.toMap()
        override val minX: Float = this@toImmutable.minX
        override val maxX: Float = this@toImmutable.maxX
        override val xStep: Float = this@toImmutable.xStep
        override val model: CartesianChartModel = this@toImmutable.model.toImmutable()

        override fun getYRange(axisPosition: AxisPosition.Vertical?): ChartValues.YRange =
            yRanges[axisPosition] ?: yRanges.getValue(null)
    }
