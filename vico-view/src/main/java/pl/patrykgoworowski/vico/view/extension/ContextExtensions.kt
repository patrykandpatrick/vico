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

package pl.patrykgoworowski.vico.view.extension

import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.os.Build
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import pl.patrykgoworowski.vico.core.Colors

internal val Context.density: Float
    get() = resources.displayMetrics.density

internal val Context.fontScale: Float
    get() = resources.displayMetrics.scaledDensity

internal val Context.isLtr: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        resources.configuration.layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR
    } else true

val Context.isDarkMode: Boolean
    get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

internal val Context.colors: Colors
    get() = if (isDarkMode) {
        Colors.Dark
    } else {
        Colors.Light
    }

fun Context.getColorCompat(@ColorRes colorRes: Int) =
    ContextCompat.getColor(this, colorRes)

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

fun Context.getThemeAttribute(@AttrRes attr: Int): TypedArray {
    val tempArray = IntArray(1)
    tempArray[0] = attr
    return obtainStyledAttributes(null, tempArray)
}
