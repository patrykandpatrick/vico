/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.chart.entry.collection

import pl.patrykgoworowski.vico.core.entry.ChartEntry
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

public open class EntryModelCalculator {

    public val stackedMap: HashMap<Float, Float> = HashMap()

    private var _minX: Float? = null
    private var _maxX: Float? = null
    private var _minY: Float? = null
    private var _maxY: Float? = null
    private var _step: Float? = null
    private var _stackedMinY: Float? = null
    private var _stackedMaxY: Float? = null

    public val minX: Float
        get() = _minX ?: 0f

    public val maxX: Float
        get() = _maxX ?: 0f

    public val minY: Float
        get() = _minY ?: 0f

    public val maxY: Float
        get() = _maxY ?: 0f

    public val step: Float
        get() = _step ?: 0f

    public val stackedMinY: Float
        get() = _stackedMinY ?: 0f

    public val stackedMaxY: Float
        get() = _stackedMaxY ?: 0f

    public fun resetValues() {
        _minX = null
        _maxX = null
        _minY = null
        _maxY = null
        _step = null
        _stackedMinY = null
        _stackedMaxY = null
        stackedMap.clear()
    }

    public fun calculateData(data: List<List<ChartEntry>>) {
        resetValues()
        calculateMinMax(data)
    }

    protected open fun calculateMinMax(data: List<List<ChartEntry>>) {
        data.forEach { entryCollection ->
            entryCollection.forEach { entry ->
                _minX = _minX?.coerceAtMost(entry.x) ?: entry.x
                _maxX = _maxX?.coerceAtLeast(entry.x) ?: entry.x
                _minY = _minY?.coerceAtMost(entry.y) ?: entry.y
                _maxY = _maxY?.coerceAtLeast(entry.y) ?: entry.y
                stackedMap[entry.x] = stackedMap.getOrElse(entry.x) { 0f } + entry.y
            }
            calculateStep(entryCollection)
        }
        stackedMap.values.forEach { y ->
            _stackedMinY = min(_stackedMinY ?: y, y)
            _stackedMaxY = max(_stackedMaxY ?: y, y)
        }
    }

    private fun calculateStep(entries: Collection<ChartEntry>) {
        val iterator = entries.iterator()
        var currentEntry: ChartEntry
        var previousEntry: ChartEntry? = null
        while (iterator.hasNext()) {
            currentEntry = iterator.next()
            previousEntry?.let { prevEntry ->
                val difference = abs(currentEntry.x - prevEntry.x)
                _step = min(_step ?: difference, difference)
            }

            previousEntry = currentEntry
        }
        if (_step == NO_VALUE) _step = 1f
    }

    private companion object {
        private const val NO_VALUE = -1f
    }
}
