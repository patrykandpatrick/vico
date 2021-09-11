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

package pl.patrykgoworowski.vico.app.data

import pl.patrykgoworowski.vico.core.entry.FloatEntry
import pl.patrykgoworowski.vico.core.entry.entryOf

class RandomEntriesGenerator(
    private val xRange: IntRange = 0..10,
    private val yRange: IntRange = 0..20
) {

    fun generateRandomEntries(): List<FloatEntry> {
        val result = ArrayList<FloatEntry>()
        val yLength = yRange.last - yRange.first
        // val xCount = (Math.random() * (xRange.last - xRange.first)).toInt()
        // for (x in 0..xCount) {
        for (x in xRange) {
            result += entryOf(x.toFloat(), (Math.random() * yLength).toFloat())
        }
        return result
    }
}
