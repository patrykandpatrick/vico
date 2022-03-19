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
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import pl.patrykgoworowski.vico.core.DEF_LABEL_LINE_COUNT
import pl.patrykgoworowski.vico.core.DefaultDimens.TEXT_COMPONENT_TEXT_SIZE
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.dimension.Margins
import pl.patrykgoworowski.vico.core.component.dimension.Padding
import pl.patrykgoworowski.vico.core.context.DrawContext
import pl.patrykgoworowski.vico.core.context.MeasureContext
import pl.patrykgoworowski.vico.core.dimensions.MutableDimensions
import pl.patrykgoworowski.vico.core.dimensions.emptyDimensions
import pl.patrykgoworowski.vico.core.draw.withCanvas
import pl.patrykgoworowski.vico.core.extension.copy
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.piRad
import pl.patrykgoworowski.vico.core.extension.rotate
import pl.patrykgoworowski.vico.core.extension.translate
import pl.patrykgoworowski.vico.core.text.getBounds
import pl.patrykgoworowski.vico.core.text.staticLayout
import pl.patrykgoworowski.vico.core.text.widestLineWidth

private const val TEXT_MEASUREMENT_CHAR = ""
private const val LAYOUT_KEY_PREFIX = "layout_"

/**
 * The component capable of rendering text directly on [android.graphics.Canvas].
 * It uses [StaticLayout] under the hood for text rendering. It supports:
 * - Multiple lines of text with automatic line breaking.
 * - Text truncation.
 * - [android.text.Spanned] text.
 * - Text rotation with [rotationDegrees].
 * - Text background with padding. Any [Component] can be used as text’s background.
 * - Text margins.
 *
 * @see [buildTextComponent]
 */
public open class TextComponent protected constructor() : Padding, Margins {

    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val tempMeasureBounds = RectF()

    /**
     * The text’s color.
     */
    public var color: Int by textPaint::color

    /**
     * The [Typeface] of the text.
     */
    public var typeface: Typeface? by textPaint::typeface

    /**
     * The clockwise rotation of this text relatively to its center.
     * [1f] equals to 1° of rotation.
     */
    public var rotationDegrees: Float = 0f

    /**
     * The size of the text in the sp unit.
     */
    public var textSizeSp: Float = 0f

    /**
     * The type of text truncation used when text’s width outranges available space.
     * By default text will be truncated at the end with “…”.
     */
    public var ellipsize: TextUtils.TruncateAt? = TextUtils.TruncateAt.END

    /**
     * The maximum count of lines used by the text.
     * For performance reasons during measurement phase the text’s height will be equal to
     * height of single line of text × [lineCount] whether or not rendered text will use it.
     */
    public var lineCount: Int = DEF_LABEL_LINE_COUNT

    /**
     * The background rendered behind the text. The background’s padding can be specified in [padding].
     * @see [padding]
     */
    public var background: Component? = null

    /**
     * The padding (space) between each side of the text and the [background].
     * It is applied whether or not [background] is null.
     */
    override var padding: MutableDimensions = emptyDimensions()

    /**
     * The margin (space) between each side of the [background] and the x and y coordinates.
     * It is applied whether or not [background] is null.
     */
    override var margins: MutableDimensions = emptyDimensions()

    private var layout: Layout = staticLayout("", textPaint, 0)

    /**
     * Draws the text onto [android.graphics.Canvas].
     * @param context Draw context supplied by a renderer.
     * @param text The text to be drawn.
     * @param textX The X coordinate for the text.
     * @param textY The Y coordinate for the text.
     * @param horizontalPosition The horizontal position of the text, relative to [textX].
     * @param verticalPosition The vertical position of the text, relative to [textY].
     * @param maxTextWidth The maximum available width in pixels for the text.
     */
    public fun drawText(
        context: DrawContext,
        text: CharSequence,
        textX: Float,
        textY: Float,
        horizontalPosition: HorizontalPosition = HorizontalPosition.Center,
        verticalPosition: VerticalPosition = VerticalPosition.Center,
        maxTextWidth: Int = Int.MAX_VALUE,
    ): Unit = with(context) {

        if (text.isBlank()) return
        layout = getLayout(
            text = text,
            fontScale = fontScale,
            width = (maxTextWidth - (padding.horizontalDp + margins.horizontalDp).wholePixels).coerceAtLeast(0),
        )

        val shouldRotate = rotationDegrees % 2f.piRad != 0f
        val textStartPosition = horizontalPosition.getTextStartPosition(context, textX, layout.widestLineWidth)
        val textTopPosition = verticalPosition.getTextTopPosition(context, textY, layout.height.toFloat())

        context.withCanvas {

            save()

            val bounds = layout.getBounds(tempMeasureBounds).apply {
                left -= padding.getLeftDp(isLtr).pixels
                top -= padding.topDp.pixels
                right += padding.getRightDp(isLtr).pixels
                bottom += padding.bottomDp.pixels
            }

            var xCorrection = 0f
            var yCorrection = 0f

            if (shouldRotate) {

                val boundsPostRotation = bounds.copy().rotate(rotationDegrees)
                val heightDelta = bounds.height() - boundsPostRotation.height()
                val widthDelta = bounds.width() - boundsPostRotation.width()

                xCorrection = when (horizontalPosition) {
                    HorizontalPosition.Start -> -widthDelta.half
                    HorizontalPosition.End -> widthDelta.half
                    else -> 0f
                }
                yCorrection = when (verticalPosition) {
                    VerticalPosition.Top -> -heightDelta.half
                    VerticalPosition.Bottom -> heightDelta.half
                    else -> 0f
                }
            }

            bounds.translate(
                x = textStartPosition + xCorrection,
                y = textTopPosition + yCorrection,
            )

            if (shouldRotate) {
                rotate(rotationDegrees, bounds.centerX(), bounds.centerY())
            }

            background?.draw(
                context = context,
                left = bounds.left,
                top = bounds.top,
                right = bounds.right,
                bottom = bounds.bottom,
            )

            translate(
                bounds.left + padding.getLeftDp(isLtr).pixels,
                bounds.top + padding.topDp.pixels,
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

    /**
     * Returns a width of this [TextComponent] for given [text].
     */
    public fun getWidth(
        context: MeasureContext,
        text: CharSequence,
    ): Float = getTextBounds(context, text).width()

    /**
     * Returns a height of this [TextComponent] for given [text] and available [width].
     */
    public fun getHeight(
        context: MeasureContext,
        text: CharSequence = TEXT_MEASUREMENT_CHAR,
        width: Int = Int.MAX_VALUE,
    ): Float = getTextBounds(context, text, width).height()

    /**
     * Returns the bounds ([RectF]) of this [TextComponent] for given [text] and available [width].
     */
    public fun getTextBounds(
        context: MeasureContext,
        text: CharSequence = TEXT_MEASUREMENT_CHAR,
        width: Int = Int.MAX_VALUE,
        outRect: RectF = tempMeasureBounds,
        includePadding: Boolean = true,
    ): RectF = with(context) {
        getLayout(text, fontScale, width).getBounds(outRect).apply {
            right += if (includePadding) (padding.horizontalDp + margins.horizontalDp).pixels else 0f
            bottom += if (includePadding) (padding.verticalDp + margins.verticalDp).pixels else 0f
        }.rotate(rotationDegrees)
    }

    private fun MeasureContext.getLayout(
        text: CharSequence,
        fontScale: Float,
        width: Int = Int.MAX_VALUE,
    ): StaticLayout {
        val key = LAYOUT_KEY_PREFIX + text + width + rotationDegrees
        return if (hasExtra(key)) {
            getExtra(key)
        } else {
            textPaint.textSize = textSizeSp * fontScale
            staticLayout(text, textPaint, width, maxLines = lineCount, ellipsize = ellipsize)
                .also { putExtra(key, it) }
        }
    }

    /**
     * The builder for [TextComponent].
     * @see buildTextComponent
     */
    public class Builder {

        /**
         * @see [TextComponent.color]
         */
        public var color: Int = Color.BLACK

        /**
         * @see [TextComponent.textSizeSp]
         */
        public var textSizeSp: Float = TEXT_COMPONENT_TEXT_SIZE

        /**
         * @see [TextComponent.typeface]
         */
        public var typeface: Typeface? = null

        /**
         * @see [TextComponent.rotationDegrees]
         */
        public var rotationDegrees: Float = 0f

        /**
         * @see [TextComponent.ellipsize]
         */
        public var ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END

        /**
         * @see [TextComponent.lineCount]
         */
        public var lineCount: Int = DEF_LABEL_LINE_COUNT

        /**
         * @see [TextComponent.background]
         */
        public var background: Component? = null

        /**
         * @see [TextComponent.padding]
         */
        public var padding: MutableDimensions = emptyDimensions()

        /**
         * @see [TextComponent.margins]
         */
        public var margins: MutableDimensions = emptyDimensions()

        /**
         * Creates a new instance of [TextComponent] with supplied properties.
         */
        public fun build(): TextComponent = TextComponent().apply {
            color = this@Builder.color
            textSizeSp = this@Builder.textSizeSp
            typeface = this@Builder.typeface
            rotationDegrees = this@Builder.rotationDegrees
            ellipsize = this@Builder.ellipsize
            lineCount = this@Builder.lineCount
            background = this@Builder.background
            padding.set(this@Builder.padding)
            margins.set(this@Builder.margins)
        }
    }
}

/**
 * The builder DSL for [TextComponent].
 *
 * Example usage:
 * ```
 * buildTextComponent {
 *    this.color = 0xFF000000 // Black color
 *    this.textSizeSp = 12f
 *    this.typeface = Typeface.MONOSPACE
 * }
 *```
 */
public fun buildTextComponent(block: TextComponent.Builder.() -> Unit = {}): TextComponent =
    TextComponent.Builder().apply(block).build()
