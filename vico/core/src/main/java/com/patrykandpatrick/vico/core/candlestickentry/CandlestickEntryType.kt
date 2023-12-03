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

package com.patrykandpatrick.vico.core.candlestickentry

import com.patrykandpatrick.vico.core.extension.orZero

/**
 * TODO
 */
public data class CandlestickEntryType(
    public val absoluteChange: Change,
    public val relativeChange: Change,
) {

    public enum class Change {
        Increase,
        Decrease,
        Zero,
        ;

        public companion object {

            public fun from(a: Float, b: Float): Change = when {
                a < b -> Increase
                a > b -> Decrease
                else -> Zero
            }
        }
    }

    public companion object {

        public fun fromValues(
            previousClose: Float?,
            currentClose: Float,
            currentOpen: Float,
        ): CandlestickEntryType = CandlestickEntryType(
            absoluteChange = Change.from(currentClose, currentOpen),
            relativeChange = Change.from(currentClose, previousClose.orZero),
        )
    }
}