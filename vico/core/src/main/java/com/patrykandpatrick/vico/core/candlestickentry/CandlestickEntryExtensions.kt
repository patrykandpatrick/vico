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

import com.patrykandpatrick.vico.core.extension.rangeOfOrNullRanged

/**
 * TODO
 */
public val CandlestickEntry.yRange: ClosedFloatingPointRange<Float>
    get() = minOf(low, open, close)..maxOf(high, open, close)

internal inline val Iterable<CandlestickEntry>.yRange: ClosedFloatingPointRange<Float>
    get() = rangeOfOrNullRanged { it.yRange } ?: 0f..0f

/**
 * TODO
 */
public fun candlestickEntryModelOf(vararg entries: CandlestickEntry): CandlestickEntryModel =
    candlestickEntryModelOf(entries.toList())

/**
 * TODO
 */
public fun candlestickEntryModelOf(entries: List<CandlestickEntry>): CandlestickEntryModel =
    CandlestickEntryModelProducer(entries)
        .getModel()
