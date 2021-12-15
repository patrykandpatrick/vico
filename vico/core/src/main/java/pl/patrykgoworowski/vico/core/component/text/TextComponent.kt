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

package pl.patrykgoworowski.vico.core.component.text

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import pl.patrykgoworowski.vico.core.DEF_LABEL_LINE_COUNT
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.dimension.Margins
import pl.patrykgoworowski.vico.core.component.dimension.Padding
import pl.patrykgoworowski.vico.core.debug.DebugHelper
import pl.patrykgoworowski.vico.core.dimensions.MutableDimensions
import pl.patrykgoworowski.vico.core.dimensions.emptyDimensions
import pl.patrykgoworowski.vico.core.draw.DrawContext
import pl.patrykgoworowski.vico.core.draw.withCanvas
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.lineHeight
import pl.patrykgoworowski.vico.core.extension.piRad
import pl.patrykgoworowski.vico.core.layout.MeasureContext
import pl.patrykgoworowski.vico.core.text.staticLayout
import pl.patrykgoworowski.vico.core.text.widestLineWidth
import kotlin.collections.HashMap
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

public typealias OnPreDrawListener = (
    context: DrawContext,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
) -> Unit

public open class TextComponent(
    color: Int = Color.BLACK,
    public var textSizeSp: Float = 12f,
    public val ellipsize: TextUtils.TruncateAt? = TextUtils.TruncateAt.END,
    public val lineCount: Int = DEF_LABEL_LINE_COUNT,
    public open var background: Component? = null,
    override val padding: MutableDimensions = emptyDimensions(),
    override val margins: MutableDimensions = emptyDimensions(),
) : Padding, Margins {

    public val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    public val lineHeight: Int
        get() = textPaint.lineHeight.roundToInt()

    public val allLinesHeight: Int
        get() = lineHeight * lineCount

    public var color: Int by textPaint::color
    public var typeface: Typeface by textPaint::typeface
    public var rotationDegrees: Float = 0f
    private var layout: StaticLayout = staticLayout("", textPaint, 0)

    private val layoutCache = HashMap<Int, StaticLayout>()

    init {
        textPaint.color = color
        textPaint.typeface = Typeface.MONOSPACE
    }

    public fun drawText(
        context: DrawContext,
        text: CharSequence,
        textX: Float,
        textY: Float,
        horizontalPosition: HorizontalPosition = HorizontalPosition.Center,
        verticalPosition: VerticalPosition = VerticalPosition.Center,
        width: Int = Int.MAX_VALUE,
        onPreDraw: OnPreDrawListener? = null,
    ): Unit = with(context) {

        if (text.isBlank()) return
        layout = getLayout(text, fontScale, width - padding.horizontalDp.wholePixels)
        val layoutWidth = layout.widestLineWidth
        val layoutHeight = layout.height

        val textStartPosition = horizontalPosition
            .getTextStartPosition(context, textX, layoutWidth)
        val textTopPosition = verticalPosition
            .getTextTopPosition(context, textY, layoutHeight.toFloat())

        val bgLeft = textStartPosition - padding.getLeftDp(isLtr).pixels
        val bgTop = floor(textTopPosition - (layoutHeight.half + padding.topDp.pixels))
        val bgRight = textStartPosition + layoutWidth + padding.getRightDp(isLtr).pixels
        val bgBottom = ceil(textTopPosition + (layoutHeight.half + padding.bottomDp.pixels))

        onPreDraw?.invoke(context, bgLeft, bgTop, bgRight, bgBottom)

        background?.draw(
            context,
            left = bgLeft,
            top = bgTop,
            right = bgRight,
            bottom = bgBottom,
        )

        context.withCanvas {
            val centeredY = textTopPosition - layoutHeight.half
            save()
            if (rotationDegrees != 0f) {
                rotate(0.25f.piRad, textStartPosition, textTopPosition)
            }
            translate(textStartPosition, centeredY)
            layout.draw(this)
            restore()
        }

        DebugHelper.drawDebugBounds(
            context = context,
            left = bgLeft,
            top = bgTop,
            right = bgRight,
            bottom = bgBottom,
        )
    }

    private fun HorizontalPosition.getTextStartPosition(
        context: MeasureContext,
        baseXPosition: Float,
        width: Float,
    ): Float = with(context) {
        when (this@getTextStartPosition) {
            HorizontalPosition.Start ->
                if (isLtr) getTextLeftPosition(baseXPosition)
                else getTextRightPosition(baseXPosition, width)
            HorizontalPosition.Center ->
                baseXPosition - width.half
            HorizontalPosition.End ->
                if (isLtr) getTextRightPosition(baseXPosition, width)
                else getTextLeftPosition(baseXPosition)
        }
    }

    private fun MeasureContext.getTextLeftPosition(baseXPosition: Float): Float =
        baseXPosition + padding.getLeftDp(isLtr).pixels + margins.getLeftDp(isLtr).pixels

    private fun MeasureContext.getTextRightPosition(baseXPosition: Float, width: Float): Float =
        baseXPosition - (padding.getRightDp(isLtr).pixels + margins.getRightDp(isLtr).pixels + width)

    @JvmName("getTextTopPositionExt")
    private fun VerticalPosition.getTextTopPosition(
        context: MeasureContext,
        textY: Float,
        layoutHeight: Float,
    ): Float = with(context) {
        when (this@getTextTopPosition) {
            VerticalPosition.Top ->
                textY + layoutHeight.half + padding.topDp.pixels + margins.topDp.pixels
            VerticalPosition.Center ->
                textY
            VerticalPosition.Bottom ->
                textY - (layoutHeight.half + padding.bottomDp.pixels + margins.bottomDp.pixels)
        }
    }

    public fun getTextTopPosition(
        context: MeasureContext,
        verticalPosition: VerticalPosition,
        textY: Float,
        layoutHeight: Float,
    ): Float = verticalPosition.getTextTopPosition(context, textY, layoutHeight)

    public fun getWidth(
        context: MeasureContext,
        text: CharSequence,
    ): Float = with(context) {
        getLayout(text, fontScale).widestLineWidth +
            padding.horizontalDp.pixels +
            margins.horizontalDp.pixels
    }

    public fun getHeight(
        context: MeasureContext,
        text: CharSequence = TEXT_MEASUREMENT_CHAR,
        width: Int = Int.MAX_VALUE,
        includePadding: Boolean = true,
        includeMargin: Boolean = true,
    ): Float = with(context) {
        getLayout(text, fontScale, width).height +
            (if (includePadding) padding.verticalDp.pixels else 0f) +
            (if (includeMargin) margins.verticalDp.pixels else 0f)
    }

    public fun clearLayoutCache() {
        layoutCache.clear()
    }

    private fun getLayout(
        text: CharSequence,
        fontScale: Float,
        width: Int = Int.MAX_VALUE,
    ): StaticLayout =
        layoutCache.getOrPut(arrayOf(text, fontScale, width).contentHashCode()) {
            textPaint.textSize = textSizeSp * fontScale
            staticLayout(text, textPaint, width, maxLines = lineCount, ellipsize = ellipsize)
        }

    private companion object {
        const val TEXT_MEASUREMENT_CHAR = ""
    }
}
