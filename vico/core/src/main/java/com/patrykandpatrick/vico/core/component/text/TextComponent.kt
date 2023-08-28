/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.component.text

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.patrykandpatrick.vico.core.DEF_LABEL_LINE_COUNT
import com.patrykandpatrick.vico.core.DefaultDimens.TEXT_COMPONENT_TEXT_SIZE
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.dimension.Margins
import com.patrykandpatrick.vico.core.component.dimension.Padding
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.context.getOrPutExtra
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions
import com.patrykandpatrick.vico.core.draw.withCanvas
import com.patrykandpatrick.vico.core.extension.copy
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.lineHeight
import com.patrykandpatrick.vico.core.extension.piRad
import com.patrykandpatrick.vico.core.extension.rotate
import com.patrykandpatrick.vico.core.extension.translate
import com.patrykandpatrick.vico.core.text.getBounds
import com.patrykandpatrick.vico.core.text.staticLayout
import com.patrykandpatrick.vico.core.text.widestLineWidth
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val LAYOUT_KEY_PREFIX = "layout_"
private const val DEF_LAYOUT_SIZE = 100000

/**
 * Uses [Canvas] to render text. This class utilizes [StaticLayout] and supports the following:
 *
 * - multi-line text with automatic line breaking
 * - text truncation
 * - [Spanned]
 * - text rotation
 * - text backgrounds (any [Component])
 * - margins and padding
 *
 * @see [textComponent]
 */
public open class TextComponent protected constructor() : Padding, Margins {

    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val tempMeasureBounds = RectF()

    /**
     * The text’s color.
     */
    public var color: Int by textPaint::color

    /**
     * The [Typeface] for the text.
     */
    public var typeface: Typeface? by textPaint::typeface

    /**
     * The font size (in sp).
     */
    public var textSizeSp: Float = 0f

    /**
     * The type of text truncation to be used when the text’s width exceeds the amount of available space. By default,
     * text is truncated at the end, and an ellipsis (…) is used.
     */
    public var ellipsize: TextUtils.TruncateAt? = TextUtils.TruncateAt.END

    /**
     * The maximum number of lines for the text. For performance reasons, during the measurement phase, it is presumed
     * that the actual number of lines is equal to this value.
     */
    public var lineCount: Int = DEF_LABEL_LINE_COUNT

    /**
     * The text’s background. Use [padding] to set the padding between the text and the background.
     *
     * @see [padding]
     */
    public var background: Component? = null

    /**
     * The text alignment.
     */
    @Deprecated("Use `textAlignment` instead.")
    public var textAlign: Paint.Align
        get() = textAlignment.equivLtrPaintAlign
        set(value) { textAlignment = value.equivLtrLayoutAlignment }

    /**
     * The text alignment.
     */
    public var textAlignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL

    /**
     * The padding between the text and the background. This is applied even if [background] is null.
     *
     * @see [background]
     */
    override var padding: MutableDimensions = emptyDimensions()

    /**
     * The margins around the background. This is applied even if [background] is null.
     *
     * @see [background]
     */
    override var margins: MutableDimensions = emptyDimensions()

    private var layout: Layout = staticLayout("", textPaint, 0)

    /**
     * Uses [Canvas] to draw this [TextComponent].
     *
     * @param context holds environment data.
     * @param text the text to be drawn.
     * @param textX the _x_ coordinate for the text.
     * @param textY the _y_ coordinate for the text.
     * @param horizontalPosition the horizontal position of the text, relative to [textX].
     * @param verticalPosition the vertical position of the text, relative to [textY].
     * @param maxTextWidth the maximum width available for the text (in pixels).
     * @param maxTextHeight the maximum height available for the text (in pixels).
     * @param rotationDegrees the rotation of the text (in degrees).
     */
    public fun drawText(
        context: DrawContext,
        text: CharSequence,
        textX: Float,
        textY: Float,
        horizontalPosition: HorizontalPosition = HorizontalPosition.Center,
        verticalPosition: VerticalPosition = VerticalPosition.Center,
        maxTextWidth: Int = DEF_LAYOUT_SIZE,
        maxTextHeight: Int = DEF_LAYOUT_SIZE,
        rotationDegrees: Float = 0f,
    ): Unit = with(context) {
        if (text.isBlank()) return
        layout = getLayout(text, maxTextWidth, maxTextHeight, rotationDegrees)

        val shouldRotate = rotationDegrees % 2f.piRad != 0f
        val textStartPosition = horizontalPosition.getTextStartPosition(context, textX, layout.widestLineWidth)
        val textTopPosition = verticalPosition.getTextTopPosition(context, textY, layout.height.toFloat())

        context.withCanvas {
            save()

            val bounds = layout.getBounds(tempMeasureBounds)
            val textAlignmentCorrection = getTextAlignmentCorrection(bounds.width())

            with(receiver = bounds) {
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
                    HorizontalPosition.Start -> widthDelta.half
                    HorizontalPosition.End -> -widthDelta.half
                    else -> 0f
                } * context.layoutDirectionMultiplier

                yCorrection = when (verticalPosition) {
                    VerticalPosition.Top -> heightDelta.half
                    VerticalPosition.Bottom -> -heightDelta.half
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
                bounds.left + padding.getLeftDp(isLtr).pixels + textAlignmentCorrection,
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
                if (isLtr) getTextRightPosition(baseXPosition, width) else getTextLeftPosition(baseXPosition)
            HorizontalPosition.Center ->
                baseXPosition - width.half
            HorizontalPosition.End ->
                if (isLtr) getTextLeftPosition(baseXPosition) else getTextRightPosition(baseXPosition, width)
        }
    }

    private fun MeasureContext.getTextLeftPosition(baseXPosition: Float): Float =
        baseXPosition + padding.getLeftDp(isLtr).pixels + margins.getLeftDp(isLtr).pixels

    private fun MeasureContext.getTextRightPosition(
        baseXPosition: Float,
        width: Float,
    ): Float = baseXPosition - padding.getRightDp(isLtr).pixels - margins.getRightDp(isLtr).pixels - width

    private fun getTextAlignmentCorrection(width: Float): Float {
        val ltrAlignment = if (layout.getParagraphDirection(0) == Layout.DIR_LEFT_TO_RIGHT) {
            textAlignment
        } else {
            when (textAlignment) {
                Layout.Alignment.ALIGN_NORMAL -> Layout.Alignment.ALIGN_OPPOSITE
                Layout.Alignment.ALIGN_OPPOSITE -> Layout.Alignment.ALIGN_NORMAL
                Layout.Alignment.ALIGN_CENTER -> Layout.Alignment.ALIGN_CENTER
            }
        }
        return when (ltrAlignment) {
            Layout.Alignment.ALIGN_NORMAL -> 0f
            Layout.Alignment.ALIGN_OPPOSITE -> width - layout.width
            Layout.Alignment.ALIGN_CENTER -> (width - layout.width).half
        }
    }

    @JvmName("getTextTopPositionExt")
    private fun VerticalPosition.getTextTopPosition(
        context: MeasureContext,
        textY: Float,
        layoutHeight: Float,
    ): Float = with(context) {
        textY + when (this@getTextTopPosition) {
            VerticalPosition.Top -> -layoutHeight - padding.bottomDp.pixels - margins.bottomDp.pixels
            VerticalPosition.Center -> -layoutHeight.half
            VerticalPosition.Bottom -> padding.topDp.pixels + margins.topDp.pixels
        }
    }

    /**
     * Returns the width of this [TextComponent] for the given [text] and the available [width] and [height]. [pad]
     * defines whether to extend [text] by such a number of blank lines that it has [lineCount] lines.
     */
    public fun getWidth(
        context: MeasureContext,
        text: CharSequence? = null,
        width: Int = DEF_LAYOUT_SIZE,
        height: Int = DEF_LAYOUT_SIZE,
        rotationDegrees: Float = 0f,
        pad: Boolean = text == null,
    ): Float = getTextBounds(
        context = context,
        text = text,
        width = width,
        height = height,
        rotationDegrees = rotationDegrees,
        pad = pad,
    ).width()

    /**
     * Returns the height of this [TextComponent] for the given [text] and the available [width] and [height]. [pad]
     * defines whether to extend [text] by such a number of blank lines that it has [lineCount] lines.
     */
    public fun getHeight(
        context: MeasureContext,
        text: CharSequence? = null,
        width: Int = DEF_LAYOUT_SIZE,
        height: Int = DEF_LAYOUT_SIZE,
        rotationDegrees: Float = 0f,
        pad: Boolean = text == null,
    ): Float = getTextBounds(
        context = context,
        text = text,
        width = width,
        height = height,
        rotationDegrees = rotationDegrees,
        pad = pad,
    ).height()

    /**
     * Returns the bounds ([RectF]) of this [TextComponent] for the given [text] and the available [width] and [height].
     * [pad] defines whether to extend [text] by such a number of blank lines that it has [lineCount] lines.
     */
    public fun getTextBounds(
        context: MeasureContext,
        text: CharSequence? = null,
        width: Int = DEF_LAYOUT_SIZE,
        height: Int = DEF_LAYOUT_SIZE,
        outRect: RectF = tempMeasureBounds,
        includePaddingAndMargins: Boolean = true,
        rotationDegrees: Float = 0f,
        pad: Boolean = text == null,
    ): RectF = with(context) {
        var measuredText = text?.toString().orEmpty()
        if (pad) repeat((lineCount - measuredText.lines().size).coerceAtLeast(0)) { measuredText += '\n' }
        getLayout(measuredText, width, height, rotationDegrees)
            .getBounds(outRect)
            .apply {
                if (includePaddingAndMargins) {
                    right += padding.horizontalDp.pixels
                    bottom += padding.verticalDp.pixels
                }
            }
            .rotate(rotationDegrees)
            .apply {
                if (includePaddingAndMargins) {
                    right += margins.horizontalDp.pixels
                    bottom += margins.verticalDp.pixels
                }
            }
    }

    private fun MeasureContext.getLayout(
        text: CharSequence,
        width: Int = DEF_LAYOUT_SIZE,
        height: Int = DEF_LAYOUT_SIZE,
        rotationDegrees: Float = 0f,
    ): StaticLayout {
        val widthWithoutMargins = width - margins.horizontalDp.wholePixels
        val heightWithoutMargins = height - margins.verticalDp.wholePixels

        val correctedWidth = (
            when {
                rotationDegrees % 1f.piRad == 0f -> widthWithoutMargins
                rotationDegrees % .5f.piRad == 0f -> heightWithoutMargins
                else -> {
                    val cumulatedHeight = lineCount * textPaint.lineHeight + padding.verticalDp.wholePixels
                    val alpha = Math.toRadians(rotationDegrees.toDouble())
                    val absSinAlpha = sin(alpha).absoluteValue
                    val absCosAlpha = cos(alpha).absoluteValue
                    val basedOnWidth = (widthWithoutMargins - cumulatedHeight * absSinAlpha) / absCosAlpha
                    val basedOnHeight = (heightWithoutMargins - cumulatedHeight * absCosAlpha) / absSinAlpha
                    min(basedOnWidth, basedOnHeight).toInt()
                }
            } - padding.horizontalDp.wholePixels
            ).coerceAtLeast(0)

        val key = LAYOUT_KEY_PREFIX + text + correctedWidth + rotationDegrees + textPaint.hashCode()
        return getOrPutExtra(key = key) {
            textPaint.textSize = spToPx(textSizeSp)
            staticLayout(
                source = text,
                paint = textPaint,
                width = correctedWidth,
                maxLines = lineCount,
                ellipsize = ellipsize,
                align = textAlignment,
            )
        }
    }

    /**
     * The builder for [TextComponent].
     * @see textComponent
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
         * @see [TextComponent.textAlign]
         */
        @Deprecated("Use `textAlignment` instead.")
        public var textAlign: Paint.Align
            get() = textAlignment.equivLtrPaintAlign
            set(value) { textAlignment = value.equivLtrLayoutAlignment }

        /**
         * @see TextComponent.textAlignment
         */
        public var textAlignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL

        /**
         * @see [TextComponent.padding]
         */
        public var padding: MutableDimensions = emptyDimensions()

        /**
         * @see [TextComponent.margins]
         */
        public var margins: MutableDimensions = emptyDimensions()

        /**
         * Creates a new instance of [TextComponent] with the supplied properties.
         */
        public fun build(): TextComponent = TextComponent().apply {
            color = this@Builder.color
            textSizeSp = this@Builder.textSizeSp
            typeface = this@Builder.typeface
            ellipsize = this@Builder.ellipsize
            lineCount = this@Builder.lineCount
            background = this@Builder.background
            textAlignment = this@Builder.textAlignment
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
 * textComponent {
 *    this.color = 0xFF000000 // This corresponds to #000000, which is black.
 *    this.textSizeSp = 12f
 *    this.typeface = Typeface.MONOSPACE
 * }
 *```
 */
public inline fun textComponent(block: TextComponent.Builder.() -> Unit = {}): TextComponent =
    TextComponent.Builder().apply(block).build()

private val Paint.Align.equivLtrLayoutAlignment
    get() = when (this) {
        Paint.Align.LEFT -> Layout.Alignment.ALIGN_NORMAL
        Paint.Align.CENTER -> Layout.Alignment.ALIGN_CENTER
        Paint.Align.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
    }

private val Layout.Alignment.equivLtrPaintAlign
    get() = when (this) {
        Layout.Alignment.ALIGN_NORMAL -> Paint.Align.LEFT
        Layout.Alignment.ALIGN_OPPOSITE -> Paint.Align.RIGHT
        Layout.Alignment.ALIGN_CENTER -> Paint.Align.CENTER
    }
