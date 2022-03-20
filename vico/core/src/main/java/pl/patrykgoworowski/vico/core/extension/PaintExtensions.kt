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

package pl.patrykgoworowski.vico.core.extension

import android.graphics.Paint

private val fm: Paint.FontMetrics = Paint.FontMetrics()

/**
 * Returns the height of a single line of text.
 */
public val Paint.lineHeight: Float
    get() {
        getFontMetrics(fm)
        return fm.bottom - fm.top + fm.leading
    }

/**
 * Returns the height of text.
 */
public val Paint.textHeight: Float
    get() {
        getFontMetrics(fm)
        return fm.descent - fm.ascent
    }

/**
 * Returns the width of the provided text.
 */
public fun Paint.measureText(text: CharSequence): Float =
    measureText(text, 0, text.length)
