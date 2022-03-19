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
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import pl.patrykgoworowski.vico.view.extension.density

private val typeCompatTypedValue = TypedValue()
private val rawValueTypedValue = TypedValue()
private val lock = Any()

/**
 * Calls the given function block with this [TypedArray] as its argument, then recycles this [TypedArray].
 */
public inline fun <R> TypedArray.use(block: (TypedArray) -> R): R =
    block(this).also { recycle() }

/**
 * Retrieves the color at the given index.
 */
public fun TypedArray.getColor(
    @StyleableRes index: Int,
    @ColorInt defaultColor: Int = Color.TRANSPARENT,
): Int = getColor(index, defaultColor)

public fun TypedArray.getRawDimension(
    context: Context,
    @StyleableRes index: Int,
    defaultValue: Float
): Float = synchronized(lock) {
    if (getValue(index, rawValueTypedValue)) {
        rawValueTypedValue.getDimension(context.resources.displayMetrics) / context.density
        TypedValue.complexToFloat(rawValueTypedValue.data)
    } else {
        defaultValue
    }
}

public fun TypedArray.getNestedTypedArray(
    context: Context,
    @StyleableRes resourceId: Int,
    @StyleableRes styleableResourceId: IntArray,
): TypedArray =
    getResourceId(resourceId, 0)
        .let { resId -> context.obtainStyledAttributes(resId, styleableResourceId) }

/**
 * Retrieves the fraction at the given index as a [Float].
 */
public fun TypedArray.getFraction(@StyleableRes index: Int, defaultValue: Float = -1f): Float =
    getFraction(index, 1, 1, defaultValue)

/**
 * Returns a boolean indicating whether the value at the given index is a fraction.
 */
public fun TypedArray.isFraction(@StyleableRes index: Int): Boolean =
    getTypeCompat(index) == TypedValue.TYPE_FRACTION

/**
 * Returns the type of the value at the given index.
 */
public fun TypedArray.getTypeCompat(@StyleableRes index: Int): Int = synchronized(lock) {
    getValue(index, typeCompatTypedValue)
    typeCompatTypedValue.type
}
