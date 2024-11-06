/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.views.common.theme

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import com.patrykandpatrick.vico.views.common.density

private val typeCompatTypedValue = TypedValue()
private val rawValueTypedValue = TypedValue()
private val lock = Any()

internal inline fun <R> TypedArray.use(block: (TypedArray) -> R): R = block(this).also { recycle() }

internal fun TypedArray.getColorExtended(
  @StyleableRes index: Int,
  @ColorInt defaultColor: Int = Color.TRANSPARENT,
): Int = getColor(index, defaultColor)

internal fun TypedArray.getRawDimension(
  context: Context,
  @StyleableRes index: Int,
  defaultValue: Float,
): Float = getRawDimensionOrNull(context, index) ?: defaultValue

internal fun TypedArray.getRawDimensionOrNull(context: Context, @StyleableRes index: Int): Float? =
  synchronized(lock) {
    if (getValue(index, rawValueTypedValue)) {
      rawValueTypedValue.getDimension(context.resources.displayMetrics) / context.density
      TypedValue.complexToFloat(rawValueTypedValue.data)
    } else {
      null
    }
  }

internal fun TypedArray.getNestedTypedArray(
  context: Context,
  @StyleableRes resourceId: Int,
  @StyleableRes styleableResourceId: IntArray,
): TypedArray =
  getResourceId(resourceId, 0).let { resId ->
    context.obtainStyledAttributes(resId, styleableResourceId)
  }

internal fun TypedArray.getFraction(@StyleableRes index: Int, defaultValue: Float = -1f): Float =
  getFraction(index, 1, 1, defaultValue)

internal fun TypedArray.isFraction(@StyleableRes index: Int): Boolean =
  getTypeCompat(index) == TypedValue.TYPE_FRACTION

private fun TypedArray.getTypeCompat(@StyleableRes index: Int): Int =
  synchronized(lock) {
    getValue(index, typeCompatTypedValue)
    typeCompatTypedValue.type
  }
