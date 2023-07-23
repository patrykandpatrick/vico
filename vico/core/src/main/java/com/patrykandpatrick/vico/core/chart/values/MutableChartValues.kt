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

package com.patrykandpatrick.vico.core.chart.values

import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.EntryModel
import com.patrykandpatrick.vico.core.extension.orZero

/**
 * An implementation of [ChartValues] whose every property is mutable.
 */
public class MutableChartValues : ChartValues {

    private var _minX: Float? = null

    private var _maxX: Float? = null

    private var _xStep: Float? = null

    private var _minY: Float? = null

    private var _maxY: Float? = null

    override val minX: Float
        get() = _minX.orZero

    override val maxX: Float
        get() = _maxX.orZero

    override val xStep: Float
        get() = _xStep ?: 1f

    override val minY: Float
        get() = _minY.orZero

    override val maxY: Float
        get() = _maxY.orZero

    override var entryModel: EntryModel<*>? = null

    /**
     * Returns `true` if all values have been set and at least one call to [tryUpdate] or [set] has been made.
     */
    public val hasValuesSet: Boolean
        get() = _minX != null || _maxX != null || _minY != null || _maxY != null

    /**
     * Attempts to update the stored values to the provided values.
     * [MutableChartValues.minX] and [MutableChartValues.minY] can be updated to a lower value.
     * [MutableChartValues.maxX] and [MutableChartValues.maxY] can be updated to a higher value.
     * [MutableChartValues.entryModel] and [MutableChartValues.xStep] are always updated.
     */
    public fun tryUpdate(
        minX: Float? = null,
        maxX: Float? = null,
        minY: Float? = null,
        maxY: Float? = null,
        xStep: Float? = null,
        entryModel: EntryModel<*>? = this.entryModel,
    ): MutableChartValues = apply {
        if (minX != null) _minX = if (_minX != null) minOf(this.minX, minX) else minX
        if (maxX != null) _maxX = if (_maxX != null) maxOf(this.maxX, maxX) else maxX
        if (minY != null) _minY = if (_minY != null) minOf(this.minY, minY) else minY
        if (maxY != null) _maxY = if (_maxY != null) maxOf(this.maxY, maxY) else maxY
        if (xStep != null) _xStep = xStep
        this.entryModel = entryModel
    }

    /**
     * Sets [minX], [maxX], [minY], and [maxY] to 0.
     */
    public fun reset() {
        _minX = null
        _maxX = null
        _minY = null
        _maxY = null
        _xStep = null
        entryModel = emptyChartEntryModel()
    }

    private companion object {

        fun emptyChartEntryModel(): ChartEntryModel = object : ChartEntryModel {
            override val entries: List<List<ChartEntry>> = emptyList()
            override val minX: Float = 0f
            override val maxX: Float = 0f
            override val minY: Float = 0f
            override val maxY: Float = 0f
            override val stackedPositiveY: Float = 0f
            override val stackedNegativeY: Float = 0f
            override val xGcd: Float = 1f
        }
    }
}
