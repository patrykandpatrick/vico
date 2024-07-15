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

package com.patrykandpatrick.vico.core.cartesian.marker

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.core.common.appendCompat
import java.text.DecimalFormat

/**
 * The default [CartesianMarkerValueFormatter]. The labels produced include the
 * [CartesianLayerModel.Entry] instances’ _y_ values, which are formatted via [decimalFormat] and,
 * if [colorCode] is `true`, color-coded.
 */
public open class DefaultCartesianMarkerValueFormatter(
  private val decimalFormat: DecimalFormat = DecimalFormat("#.##;−#.##"),
  private val colorCode: Boolean = true,
) : CartesianMarkerValueFormatter {
  protected open fun SpannableStringBuilder.append(y: Double, color: Int? = null) {
    if (colorCode && color != null) {
      appendCompat(
        decimalFormat.format(y),
        ForegroundColorSpan(color),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
      )
    } else {
      append(decimalFormat.format(y))
    }
  }

  protected open fun SpannableStringBuilder.append(
    target: CartesianMarker.Target,
    shorten: Boolean,
  ) {
    when (target) {
      is CandlestickCartesianLayerMarkerTarget -> {
        if (shorten) {
          append(target.entry.closing, target.closingColor)
        } else {
          append("O ")
          append(target.entry.opening, target.openingColor)
          append(", C ")
          append(target.entry.closing, target.closingColor)
          append(", L ")
          append(target.entry.low, target.lowColor)
          append(", H ")
          append(target.entry.high, target.highColor)
        }
      }
      is ColumnCartesianLayerMarkerTarget -> {
        val includeSum = target.columns.size > 1
        if (includeSum) {
          append(target.columns.sumOf { it.entry.y })
          append(" (")
        }
        target.columns.forEachIndexed { index, column ->
          append(column.entry.y, column.color)
          if (index != target.columns.lastIndex) append(", ")
        }
        if (includeSum) append(")")
      }
      is LineCartesianLayerMarkerTarget -> {
        target.points.forEachIndexed { index, point ->
          append(point.entry.y, point.color)
          if (index != target.points.lastIndex) append(", ")
        }
      }
      else -> throw IllegalArgumentException("Unexpected `CartesianMarker.Target` implementation.")
    }
  }

  override fun format(
    context: CartesianDrawContext,
    targets: List<CartesianMarker.Target>,
  ): CharSequence =
    SpannableStringBuilder().apply {
      targets.forEachIndexed { index, target ->
        append(target = target, shorten = targets.size > 1)
        if (index != targets.lastIndex) append(", ")
      }
    }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is DefaultCartesianMarkerValueFormatter &&
        decimalFormat == other.decimalFormat &&
        colorCode == other.colorCode

  override fun hashCode(): Int = 31 * decimalFormat.hashCode() + colorCode.hashCode()
}
