/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.view.theme

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.os.Build
import android.text.TextUtils
import androidx.annotation.StyleableRes
import androidx.core.content.res.ResourcesCompat
import com.patrykandpatryk.vico.core.DEF_LABEL_LINE_COUNT
import com.patrykandpatryk.vico.core.DefaultDimens.AXIS_LABEL_HORIZONTAL_PADDING
import com.patrykandpatryk.vico.core.DefaultDimens.AXIS_LABEL_VERTICAL_PADDING
import com.patrykandpatryk.vico.core.DefaultDimens.TEXT_COMPONENT_TEXT_SIZE
import com.patrykandpatryk.vico.core.component.text.TextComponent
import com.patrykandpatryk.vico.core.component.text.textComponent
import com.patrykandpatryk.vico.core.dimensions.MutableDimensions
import com.patrykandpatryk.vico.core.extension.firstNonNegativeOf
import com.patrykandpatryk.vico.view.R
import com.patrykandpatryk.vico.view.extension.defaultColors

private const val FONT_WEIGHT_NORMAL = 500

internal fun TypedArray.getTextComponent(
    context: Context
): TextComponent = use {
    val color = getColor(R.styleable.LabelStyle_labelColor, context.defaultColors.axisLabelColor.toInt())
    val background = getNestedTypedArray(
        context = context,
        resourceId = R.styleable.LabelStyle_backgroundStyle,
        styleableResourceId = R.styleable.ComponentStyle,
    ).getComponent(context)

    textComponent {
        this.color = color
        this.background = background
        this.padding = getLabelPadding(context)
        this.textSizeSp = getRawDimension(
            context = context,
            index = R.styleable.LabelStyle_android_textSize,
            defaultValue = TEXT_COMPONENT_TEXT_SIZE,
        )
        this.lineCount = getInteger(R.styleable.LabelStyle_android_maxLines, DEF_LABEL_LINE_COUNT)
        this.ellipsize = getLabelTruncateAt()
        getLabelTypeface(context)?.let { this.typeface = it }
    }
}

private fun TypedArray.getLabelTruncateAt(): TextUtils.TruncateAt {
    val int = getInt(R.styleable.LabelStyle_android_ellipsize, TextUtils.TruncateAt.END.ordinal)
    val values = TextUtils.TruncateAt.values()
    return when (int) {
        in 1..values.size -> values[int]
        else -> TextUtils.TruncateAt.END
    }
}

private fun TypedArray.getLabelTypeface(context: Context): Typeface? {
    val fontIndex = if (hasValue(R.styleable.LabelStyle_android_fontFamily)) {
        R.styleable.LabelStyle_android_fontFamily
    } else {
        R.styleable.LabelStyle_fontFamily
    }

    val fontStyle = if (hasValue(R.styleable.LabelStyle_android_fontStyle)) {
        R.styleable.LabelStyle_android_fontStyle
    } else {
        R.styleable.LabelStyle_fontStyle
    }

    val fontResId = getResourceId(fontIndex, 0)
    val fontWeight = getInteger(R.styleable.LabelStyle_android_textFontWeight, FONT_WEIGHT_NORMAL)

    val family = if (fontResId > 0) {
        ResourcesCompat.getFont(context, fontResId)
    } else {
        Typeface.MONOSPACE
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Typeface.create(family, fontWeight, fontStyle == 1)
    } else {
        family
    }
}

private fun TypedArray.getLabelPadding(context: Context): MutableDimensions {

    fun getDpDimension(@StyleableRes index: Int): Float =
        getRawDimension(context, index, -1f)

    val padding = getDpDimension(R.styleable.LabelStyle_android_padding)
    val paddingVertical = getDpDimension(R.styleable.LabelStyle_android_paddingVertical)
    val paddingHorizontal = getDpDimension(R.styleable.LabelStyle_android_paddingHorizontal)
    val paddingStart = getDpDimension(R.styleable.LabelStyle_android_paddingStart)
    val paddingTop = getDpDimension(R.styleable.LabelStyle_android_paddingTop)
    val paddingEnd = getDpDimension(R.styleable.LabelStyle_android_paddingEnd)
    val paddingBottom = getDpDimension(R.styleable.LabelStyle_android_paddingBottom)
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
