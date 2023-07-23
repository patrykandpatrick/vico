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
 * @param color TODO
 */
public sealed class CandlestickEntryType(public val color: Color) {

    /**
     * TODO
     */
    public class Filled(color: Color) : CandlestickEntryType(color)

    /**
     * TODO
     */
    public class Hollow(color: Color) : CandlestickEntryType(color)

    /**
     * TODO
     */
    public class Cross(color: Color) : CandlestickEntryType(color)

    /**
     * TODO
     */
    public enum class Color {
        Green,
        Red,
        Gray,
    }

    public companion object {

        /**
         * TODO
         */
        public fun standard(open: Float, close: Float): CandlestickEntryType = when {
            close > open -> Filled(Color.Green)
            close == open -> Cross(Color.Gray)
            else -> Filled(Color.Red)
        }

        /**
         * TODO
         */
        public fun hollow(
            previousClose: Float?,
            currentClose: Float,
            currentOpen: Float,
        ): CandlestickEntryType {
            val previousCloseOrZero = previousClose.orZero

            val color = when {
                currentClose > previousCloseOrZero -> Color.Green
                currentClose == previousCloseOrZero -> Color.Gray
                else -> Color.Red
            }

            return when {
                currentClose > currentOpen -> Hollow(color = color)
                currentClose == currentOpen -> Cross(color = color)
                else -> Filled(color = color)
            }
        }
    }
}
