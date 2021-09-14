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

package pl.patrykgoworowski.vico.app.extension

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import pl.patrykgoworowski.vico.R

val Context.flickrPink: Int
    get() = ContextCompat.getColor(this, R.color.flickr_pink)

val Context.byzantine: Int
    get() = ContextCompat.getColor(this, R.color.byzantine)

val Context.trypanPurple: Int
    get() = ContextCompat.getColor(this, R.color.trypan_purple)

val Context.purple: Int
    get() = ContextCompat.getColor(this, R.color.purple)

inline fun Context.color(resIdBlock: () -> Int): Int =
    ContextCompat.getColor(this, resIdBlock())

fun Context.getThemeColor(@AttrRes attr: Int, @ColorRes defValueRes: Int? = null): Int {
    val tempArray = IntArray(1)
    tempArray[0] = attr
    val a = obtainStyledAttributes(null, tempArray)
    return try {
        a.getColor(0, defValueRes?.let { ContextCompat.getColor(this, it) } ?: 0)
    } finally {
        a.recycle()
    }
}
