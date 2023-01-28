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

package com.patrykandpatrick.vico.core.util

import com.patrykandpatrick.vico.core.candlestickentry.CandlestickTypedEntry
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Generates randomized chart entries.
 * @param xRange the range of _x_ values.
 * @param yRange the range from which _y_ values are randomly selected.
 */
public class RandomCandlestickEntriesGenerator(
    private val xRange: IntProgression = 0..X_RANGE_TOP,
    private val yRange: IntProgression = Y_RANGE_BOTTOM..Y_RANGE_TOP,
) {
    /**
     * Generates a [List] of [CandlestickTypedEntry] instances with randomized _y_ values.
     * The size of the [List] is equal to the number of values in [xRange].
     */
    public fun generateRandomEntries(): List<CandlestickTypedEntry> {
        val result = ArrayList<CandlestickTypedEntry>()
        val yLength = yRange.last - yRange.first
        for (x in xRange) {
            val isRising = Random.nextBoolean()

            var open: Float
            var close: Float

            val openCloseLength = Random.nextFloat() * OPEN_CLOSE_FACTOR

            if (isRising) {
                close = yRange.first + Random.nextFloat() * yLength
                open = (close + openCloseLength).coerceAtMost(yRange.last.toFloat())
            } else {
                open = yRange.first + Random.nextFloat() * yLength
                close = (open + openCloseLength).coerceAtMost(yRange.last.toFloat())
            }

            val low = min(open, close) - Random.nextFloat() * LOW_HIGH_FACTOR
            val high = max(open, close) + Random.nextFloat() * LOW_HIGH_FACTOR

            result += CandlestickTypedEntry(
                x = x.toFloat(),
                low = low,
                high = high,
                open = open,
                close = close,
            )
        }
        return result
    }

    private companion object {
        const val X_RANGE_TOP = 10
        const val Y_RANGE_BOTTOM = 4
        const val Y_RANGE_TOP = 20

        const val OPEN_CLOSE_FACTOR = 10f
        const val LOW_HIGH_FACTOR = 2
    }
}
