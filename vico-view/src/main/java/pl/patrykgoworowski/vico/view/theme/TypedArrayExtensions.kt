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

package pl.patrykgoworowski.vico.view.theme

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes

public fun TypedArray.getColor(
    @StyleableRes index: Int,
    @ColorInt defaultColor: Int,
): Int = getColor(index, defaultColor)

public fun TypedArray.getDpDimension(
    context: Context,
    @StyleableRes index: Int,
    defaultValue: Float
): Float =
    (getDimension(index, -1f) / context.resources.displayMetrics.density)
        .takeIf { it >= 0f } ?: defaultValue

public fun TypedArray.getNestedTypedArray(
    context: Context,
    @StyleableRes resourceId: Int,
    @StyleableRes styleableResourceId: IntArray,
): TypedArray =
    getResourceId(resourceId, 0)
        .let { resId -> context.obtainStyledAttributes(resId, styleableResourceId) }

public fun TypedArray.getFraction(@StyleableRes index: Int, defaultValue: Float = -1f): Float =
    getFraction(index, 1, 1, defaultValue)

public fun TypedArray.isFraction(@StyleableRes index: Int): Boolean =
    getType(index) == TypedValue.TYPE_FRACTION

public fun TypedArray.isNull(@StyleableRes index: Int): Boolean =
    getType(index) == TypedValue.TYPE_NULL
