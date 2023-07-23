/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.views.theme

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.TextUtils
import androidx.annotation.StyleableRes
import androidx.core.content.res.ResourcesCompat
import com.patrykandpatrick.vico.core.DEF_LABEL_LINE_COUNT
import com.patrykandpatrick.vico.core.DefaultDimens.AXIS_LABEL_HORIZONTAL_PADDING
import com.patrykandpatrick.vico.core.DefaultDimens.AXIS_LABEL_VERTICAL_PADDING
import com.patrykandpatrick.vico.core.DefaultDimens.TEXT_COMPONENT_TEXT_SIZE
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.extension.firstNonNegativeOf
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.extension.defaultColors

private const val FONT_WEIGHT_NORMAL = 400

internal fun TypedArray.getTextComponent(
    context: Context,
): TextComponent = use {
    val color = getColor(R.styleable.TextComponentStyle_labelColor, context.defaultColors.axisLabelColor.toInt())
    val background = getNestedTypedArray(
        context = context,
        resourceId = R.styleable.TextComponentStyle_backgroundStyle,
        styleableResourceId = R.styleable.ComponentStyle,
    ).getComponent(context)

    textComponent {
        this.color = color
        this.background = background
        this.padding = getPadding(context)
        this.margins = getMargins(context)
        this.textSizeSp = getRawDimension(
            context = context,
            index = R.styleable.TextComponentStyle_android_textSize,
            defaultValue = TEXT_COMPONENT_TEXT_SIZE,
        )
        this.lineCount = getInteger(R.styleable.TextComponentStyle_android_maxLines, DEF_LABEL_LINE_COUNT)
        this.ellipsize = getTruncateAt()
        getTypeface(context)?.let { this.typeface = it }
        this.textAlign = getTextAlign()
    }
}

private fun TypedArray.getTruncateAt(): TextUtils.TruncateAt {
    val int = getInt(R.styleable.TextComponentStyle_android_ellipsize, TextUtils.TruncateAt.END.ordinal)
    val values = TextUtils.TruncateAt.values()
    return when (int) {
        in 1..values.size -> values[int]
        else -> TextUtils.TruncateAt.END
    }
}

@Suppress("MagicNumber")
private fun TypedArray.getTypeface(context: Context): Typeface? {
    val fontIndex = if (hasValue(R.styleable.TextComponentStyle_android_fontFamily)) {
        R.styleable.TextComponentStyle_android_fontFamily
    } else {
        R.styleable.TextComponentStyle_fontFamily
    }

    val fontStyle = if (hasValue(R.styleable.TextComponentStyle_android_fontStyle)) {
        R.styleable.TextComponentStyle_android_fontStyle
    } else {
        R.styleable.TextComponentStyle_fontStyle
    }

    val fontResId = getResourceId(fontIndex, 0)
    val fontWeight = getInteger(R.styleable.TextComponentStyle_android_textFontWeight, FONT_WEIGHT_NORMAL)

    val family = if (fontResId > 0) {
        ResourcesCompat.getFont(context, fontResId)
    } else {
        when (getInteger(R.styleable.TextComponentStyle_typeface, 3)) {
            0 -> Typeface.DEFAULT
            1 -> Typeface.SANS_SERIF
            2 -> Typeface.SERIF
            else -> Typeface.MONOSPACE
        }
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Typeface.create(family, fontWeight, fontStyle == 1)
    } else {
        family
    }
}

private fun TypedArray.getPadding(context: Context): MutableDimensions {
    fun getDpDimension(@StyleableRes index: Int): Float =
        getRawDimension(context, index, -1f)

    val padding = getDpDimension(R.styleable.TextComponentStyle_android_padding)
    val paddingVertical = getDpDimension(R.styleable.TextComponentStyle_android_paddingVertical)
    val paddingHorizontal = getDpDimension(R.styleable.TextComponentStyle_android_paddingHorizontal)
    val paddingStart = getDpDimension(R.styleable.TextComponentStyle_android_paddingStart)
    val paddingTop = getDpDimension(R.styleable.TextComponentStyle_android_paddingTop)
    val paddingEnd = getDpDimension(R.styleable.TextComponentStyle_android_paddingEnd)
    val paddingBottom = getDpDimension(R.styleable.TextComponentStyle_android_paddingBottom)
    return MutableDimensions(
        startDp = firstNonNegativeOf(paddingStart, paddingHorizontal, padding)
            ?: AXIS_LABEL_HORIZONTAL_PADDING.toFloat(),
        topDp = firstNonNegativeOf(paddingTop, paddingVertical, padding)
            ?: AXIS_LABEL_VERTICAL_PADDING.toFloat(),
        endDp = firstNonNegativeOf(paddingEnd, paddingHorizontal, padding)
            ?: AXIS_LABEL_HORIZONTAL_PADDING.toFloat(),
        bottomDp = firstNonNegativeOf(paddingBottom, paddingVertical, padding)
            ?: AXIS_LABEL_VERTICAL_PADDING.toFloat(),
    )
}

private fun TypedArray.getMargins(context: Context): MutableDimensions {
    fun getDpDimension(@StyleableRes index: Int): Float =
        getRawDimension(context, index, -1f)

    val padding = getDpDimension(R.styleable.TextComponentStyle_margin)
    val paddingVertical = getDpDimension(R.styleable.TextComponentStyle_marginVertical)
    val paddingHorizontal = getDpDimension(R.styleable.TextComponentStyle_marginHorizontal)
    val paddingStart = getDpDimension(R.styleable.TextComponentStyle_marginStart)
    val paddingTop = getDpDimension(R.styleable.TextComponentStyle_marginTop)
    val paddingEnd = getDpDimension(R.styleable.TextComponentStyle_marginEnd)
    val paddingBottom = getDpDimension(R.styleable.TextComponentStyle_marginBottom)
    return MutableDimensions(
        startDp = firstNonNegativeOf(paddingStart, paddingHorizontal, padding).orZero,
        topDp = firstNonNegativeOf(paddingTop, paddingVertical, padding).orZero,
        endDp = firstNonNegativeOf(paddingEnd, paddingHorizontal, padding).orZero,
        bottomDp = firstNonNegativeOf(paddingBottom, paddingVertical, padding).orZero,
    )
}

private fun TypedArray.getTextAlign(): Paint.Align {
    val values = Paint.Align.values()
    val index = getInt(
        R.styleable.TextComponentStyle_textAlign,
        Paint.Align.LEFT.ordinal,
    ).coerceAtMost(maximumValue = Paint.Align.RIGHT.ordinal)
    return values[index]
}
