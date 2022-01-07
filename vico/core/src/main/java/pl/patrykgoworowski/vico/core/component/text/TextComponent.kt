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
import android.graphics.RectF
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import kotlin.math.roundToInt
import pl.patrykgoworowski.vico.core.DEF_LABEL_LINE_COUNT
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.dimension.Margins
import pl.patrykgoworowski.vico.core.component.dimension.Padding
import pl.patrykgoworowski.vico.core.context.DrawContext
import pl.patrykgoworowski.vico.core.context.MeasureContext
import pl.patrykgoworowski.vico.core.dimensions.MutableDimensions
import pl.patrykgoworowski.vico.core.dimensions.emptyDimensions
import pl.patrykgoworowski.vico.core.draw.withCanvas
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.lineHeight
import pl.patrykgoworowski.vico.core.extension.rotate
import pl.patrykgoworowski.vico.core.extension.translate
import pl.patrykgoworowski.vico.core.text.getBounds
import pl.patrykgoworowski.vico.core.text.staticLayout
import pl.patrykgoworowski.vico.core.text.widestLineWidth

public typealias OnPreDrawListener = (
    context: DrawContext,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
) -> Unit

private const val TEXT_MEASUREMENT_CHAR = ""
private const val LAYOUT_KEY_PREFIX = "layout_"

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

    private val tempMeasureBounds = RectF()

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
    ): Unit = with(context) {

        if (text.isBlank()) return

        layout = getLayout(text, fontScale, width - padding.horizontalDp.wholePixels)
        val textStartPosition = horizontalPosition.getTextStartPosition(context, textX, layout.widestLineWidth)
        val textTopPosition = verticalPosition.getTextTopPosition(context, textY, layout.height.toFloat())

        context.withCanvas {

            save()

            val bounds = layout.getBounds(tempMeasureBounds)
            val initialBoundsWidth = bounds.width()
            val initialBoundsHeight = bounds.height()

            bounds.rotate(rotationDegrees)

            val yCorrection = when (verticalPosition) {
                VerticalPosition.Top -> (bounds.height() - initialBoundsHeight).half
                VerticalPosition.Bottom -> (initialBoundsHeight - bounds.height()).half
                else -> 0f
            }
            val xCorrection = when (horizontalPosition) {
                HorizontalPosition.Start -> (bounds.width() - initialBoundsWidth).half
                HorizontalPosition.End -> (initialBoundsWidth - bounds.width()).half
                else -> 0f
            }

            bounds.translate(
                x = textStartPosition + xCorrection,
                y = textTopPosition + yCorrection,
            )

            background?.draw(
                context = context,
                left = bounds.left - padding.getLeftDp(isLtr).pixels,
                top = bounds.top - padding.topDp.pixels,
                right = bounds.right + padding.getRightDp(isLtr).pixels,
                bottom = bounds.bottom + padding.bottomDp.pixels,
            )

            rotate(rotationDegrees, bounds.centerX(), bounds.centerY())

            translate(
                bounds.centerX() - initialBoundsWidth.half,
                bounds.centerY() - initialBoundsHeight.half,
            )

            layout.draw(this)
            restore()
        }
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
        baseXPosition - padding.getRightDp(isLtr).pixels - margins.getRightDp(isLtr).pixels - width

    @JvmName("getTextTopPositionExt")
    private fun VerticalPosition.getTextTopPosition(
        context: MeasureContext,
        textY: Float,
        layoutHeight: Float,
    ): Float = with(context) {
        when (this@getTextTopPosition) {
            VerticalPosition.Top ->
                textY + padding.topDp.pixels + margins.topDp.pixels
            VerticalPosition.Center ->
                textY - layoutHeight.half
            VerticalPosition.Bottom ->
                textY - layoutHeight - padding.bottomDp.pixels - margins.bottomDp.pixels
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
        getTextBoundsWithPadding(context, text).width() + margins.horizontalDp.pixels
    }

    public fun getHeight(
        context: MeasureContext,
        text: CharSequence = TEXT_MEASUREMENT_CHAR,
        width: Int = Int.MAX_VALUE,
    ): Float = with(context) {
        getTextBoundsWithPadding(context, text, width).height() + margins.verticalDp.pixels
    }

    public fun getTextBoundsWithPadding(
        context: MeasureContext,
        text: CharSequence = TEXT_MEASUREMENT_CHAR,
        width: Int = Int.MAX_VALUE,
        outRect: RectF = tempMeasureBounds,
        includePadding: Boolean = true,
    ): RectF = with(context) {
        getLayout(text, fontScale, width).getBounds(outRect).apply {
            right += if (includePadding) padding.horizontalDp.pixels else 0f
            bottom += if (includePadding) padding.verticalDp.pixels else 0f
        }.rotate(rotationDegrees)
    }

    private fun MeasureContext.getLayout(
        text: CharSequence,
        fontScale: Float,
        width: Int = Int.MAX_VALUE,
    ): StaticLayout {
        val key = LAYOUT_KEY_PREFIX + text
        return if (hasExtra(key)) {
            getExtra(key)
        } else {
            textPaint.textSize = textSizeSp * fontScale
            staticLayout(text, textPaint, width, maxLines = lineCount, ellipsize = ellipsize)
                .also { putExtra(key, it) }
        }
    }
}
