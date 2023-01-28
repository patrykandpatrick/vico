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

import com.patrykandpatrick.vico.core.entry.Entry

/**
 * TODO
 */
public interface CandlestickEntry : Entry {

    public override val x: Float

    /**
     * TODO
     */
    public val low: Float

    /**
     * TODO
     */
    public val high: Float

    /**
     * TODO
     */
    public val open: Float

    /**
     * TODO
     */
    public val close: Float

    /**
     * TODO
     */
    public fun withValuesAndType(
        low: Float,
        high: Float,
        open: Float,
        close: Float,
        type: CandlestickEntryType,
    ): CandlestickTypedEntry
}
