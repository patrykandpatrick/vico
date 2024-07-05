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

package com.patrykandpatrick.vico.core.common.component

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.patrykandpatrick.vico.core.common.Defaults.LABEL_LINE_COUNT
import com.patrykandpatrick.vico.core.common.Defaults.TEXT_COMPONENT_TEXT_SIZE
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.DrawContext
import com.patrykandpatrick.vico.core.common.HorizontalPosition
import com.patrykandpatrick.vico.core.common.MeasureContext
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.copy
import com.patrykandpatrick.vico.core.common.data.CacheStore
import com.patrykandpatrick.vico.core.common.getBounds
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.piRad
import com.patrykandpatrick.vico.core.common.rotate
import com.patrykandpatrick.vico.core.common.staticLayout
import com.patrykandpatrick.vico.core.common.translate
import com.patrykandpatrick.vico.core.common.widestLineWidth
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val DEF_LAYOUT_SIZE = 100000

/**
 * Uses [Canvas] to render text. This class utilizes [StaticLayout] and supports the following:
 * - multi-line text with automatic line breaking
 * - text truncation
 * - [Spanned]
 * - text rotation
 * - text backgrounds (any [Component])
 * - margins and padding
 *
 * It’s recommended to create instances via [TextComponent.build].
 */
public open class TextComponent protected constructor() {
  private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
  private val tempMeasureBounds = RectF()

  /** The text’s color. */
  public var color: Int by textPaint::color

  /** The [Typeface] for the text. */
  public var typeface: Typeface? by textPaint::typeface

  /** The font size (in sp). */
  public var textSizeSp: Float = 0f

  /**
   * The type of text truncation to be used when the text’s width exceeds the amount of available
   * space. By default, text is truncated at the end, and an ellipsis (…) is used.
   */
  public var ellipsize: TextUtils.TruncateAt? = TextUtils.TruncateAt.END

  /**
   * The maximum number of lines for the text. For performance reasons, during the measurement
   * phase, it is presumed that the actual number of lines is equal to this value.
   */
  public var lineCount: Int = LABEL_LINE_COUNT

  /**
   * The text’s background. Use [padding] to set the padding between the text and the background.
   *
   * @see [padding]
   */
  public var background: Component? = null

  /** The text alignment. */
  public var textAlignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL

  /** Defines the minimum width. */
  public var minWidth: MinWidth = MinWidth.fixed()

  /**
   * The padding between the text and the background. This is applied even if [background] is null.
   *
   * @see [background]
   */
  public var padding: Dimensions = Dimensions.Empty

  /**
   * The margins around the background. This is applied even if [background] is null.
   *
   * @see [background]
   */
  public var margins: Dimensions = Dimensions.Empty

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
  ): Unit =
    with(context) {
      if (text.isBlank()) return
      layout = getLayout(this, text, maxTextWidth, maxTextHeight, rotationDegrees)

      val shouldRotate = rotationDegrees % 2f.piRad != 0f
      val textStartPosition =
        horizontalPosition.getTextStartPosition(context, textX, layout.widestLineWidth)
      val textTopPosition =
        verticalPosition.getTextTopPosition(context, textY, layout.height.toFloat())

      context.withSavedCanvas {
        val bounds = layout.getBounds(tempMeasureBounds)
        val paddingLeft = padding.getLeftDp(isLtr).pixels
        val paddingRight = padding.getRightDp(isLtr).pixels
        val textAlignmentCorrection: Float

        with(receiver = bounds) {
          val minWidth =
            minWidth.getValue(
              context,
              this@TextComponent,
              maxTextWidth,
              maxTextHeight,
              rotationDegrees,
            ) - padding.horizontalDp.pixels
          val minWidthCorrection =
            (minWidth.coerceAtMost(layout.width.toFloat()) - width()).coerceAtLeast(0f)
          left -= minWidthCorrection.half
          right += minWidthCorrection.half
          textAlignmentCorrection = getTextAlignmentCorrection(width())
          left -= paddingLeft
          top -= padding.topDp.pixels
          right += paddingRight
          bottom += padding.bottomDp.pixels
        }

        var xCorrection = 0f
        var yCorrection = 0f

        if (shouldRotate) {
          val boundsPostRotation = bounds.copy().rotate(rotationDegrees)
          val heightDelta = bounds.height() - boundsPostRotation.height()
          val widthDelta = bounds.width() - boundsPostRotation.width()

          xCorrection =
            when (horizontalPosition) {
              HorizontalPosition.Start -> widthDelta.half
              HorizontalPosition.End -> -widthDelta.half
              else -> 0f
            } * context.layoutDirectionMultiplier

          yCorrection =
            when (verticalPosition) {
              VerticalPosition.Top -> heightDelta.half
              VerticalPosition.Bottom -> -heightDelta.half
              else -> 0f
            }
        }
        bounds.translate(x = textStartPosition + xCorrection, y = textTopPosition + yCorrection)

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
          bounds.left + paddingLeft + textAlignmentCorrection,
          bounds.top + padding.topDp.pixels,
        )

        layout.draw(this)
      }
    }

  private fun HorizontalPosition.getTextStartPosition(
    context: MeasureContext,
    baseXPosition: Float,
    width: Float,
  ): Float =
    with(context) {
      when (this@getTextStartPosition) {
        HorizontalPosition.Start ->
          if (isLtr) getTextRightPosition(baseXPosition, width)
          else getTextLeftPosition(baseXPosition)
        HorizontalPosition.Center -> baseXPosition - width.half
        HorizontalPosition.End ->
          if (isLtr) getTextLeftPosition(baseXPosition)
          else getTextRightPosition(baseXPosition, width)
      }
    }

  private fun MeasureContext.getTextLeftPosition(baseXPosition: Float): Float =
    baseXPosition + padding.getLeftDp(isLtr).pixels + margins.getLeftDp(isLtr).pixels

  private fun MeasureContext.getTextRightPosition(baseXPosition: Float, width: Float): Float =
    baseXPosition - padding.getRightDp(isLtr).pixels - margins.getRightDp(isLtr).pixels - width

  private fun getTextAlignmentCorrection(width: Float): Float {
    val ltrAlignment =
      if (layout.getParagraphDirection(0) == Layout.DIR_LEFT_TO_RIGHT) {
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
  ): Float =
    with(context) {
      textY +
        when (this@getTextTopPosition) {
          VerticalPosition.Top -> -layoutHeight - padding.bottomDp.pixels - margins.bottomDp.pixels
          VerticalPosition.Center -> -layoutHeight.half
          VerticalPosition.Bottom -> padding.topDp.pixels + margins.topDp.pixels
        }
    }

  /**
   * Returns the width of this [TextComponent] for the given [text] and the available [width] and
   * [height]. [pad] defines whether to extend [text] by such a number of blank lines that it has
   * [lineCount] lines.
   */
  public fun getWidth(
    context: MeasureContext,
    text: CharSequence? = null,
    width: Int = DEF_LAYOUT_SIZE,
    height: Int = DEF_LAYOUT_SIZE,
    rotationDegrees: Float = 0f,
    pad: Boolean = text == null,
  ): Float =
    getTextBounds(
        context = context,
        text = text,
        width = width,
        height = height,
        rotationDegrees = rotationDegrees,
        pad = pad,
      )
      .width()

  /**
   * Returns the height of this [TextComponent] for the given [text] and the available [width] and
   * [height]. [pad] defines whether to extend [text] by such a number of blank lines that it has
   * [lineCount] lines.
   */
  public fun getHeight(
    context: MeasureContext,
    text: CharSequence? = null,
    width: Int = DEF_LAYOUT_SIZE,
    height: Int = DEF_LAYOUT_SIZE,
    rotationDegrees: Float = 0f,
    pad: Boolean = text == null,
  ): Float =
    getTextBounds(
        context = context,
        text = text,
        width = width,
        height = height,
        rotationDegrees = rotationDegrees,
        pad = pad,
      )
      .height()

  /**
   * Returns the bounds ([RectF]) of this [TextComponent] for the given [text] and the available
   * [width] and [height]. [pad] defines whether to extend [text] by such a number of blank lines
   * that it has [lineCount] lines.
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
  ): RectF =
    with(context) {
      val measuredText = SpannableStringBuilder(text ?: "")
      if (pad)
        repeat((lineCount - measuredText.lines().size).coerceAtLeast(0)) {
          measuredText.append('\n')
        }
      val layout = getLayout(this, measuredText, width, height, rotationDegrees)
      layout
        .getBounds(outRect)
        .apply {
          val minWidth =
            minWidth.getValue(context, this@TextComponent, width, height, rotationDegrees) -
              padding.horizontalDp.pixels
          right = right.coerceAtLeast(minWidth).coerceAtMost(layout.width.toFloat())
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

  private fun getLayout(
    context: MeasureContext,
    text: CharSequence,
    width: Int = DEF_LAYOUT_SIZE,
    height: Int = DEF_LAYOUT_SIZE,
    rotationDegrees: Float = 0f,
  ) =
    context.run {
      val widthWithoutMargins = width - margins.horizontalDp.wholePixels
      val heightWithoutMargins = height - margins.verticalDp.wholePixels

      val correctedWidth =
        (when {
            rotationDegrees % 1f.piRad == 0f -> widthWithoutMargins
            rotationDegrees % .5f.piRad == 0f -> heightWithoutMargins
            else -> {
              val cumulatedHeight =
                lineCount * textPaint.lineHeight + padding.verticalDp.wholePixels
              val alpha = Math.toRadians(rotationDegrees.toDouble())
              val absSinAlpha = sin(alpha).absoluteValue
              val absCosAlpha = cos(alpha).absoluteValue
              val basedOnWidth = (widthWithoutMargins - cumulatedHeight * absSinAlpha) / absCosAlpha
              val basedOnHeight =
                (heightWithoutMargins - cumulatedHeight * absCosAlpha) / absSinAlpha
              min(basedOnWidth, basedOnHeight).toInt()
            }
          } - padding.horizontalDp.wholePixels)
          .coerceAtLeast(0)

      cacheStore.getOrSet(
        cacheKeyNamespace,
        text.hashCode(),
        textPaint.hashCode(),
        textSizeSp,
        correctedWidth,
        lineCount,
        ellipsize,
        textAlignment,
      ) {
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

  private inline fun DrawContext.withSavedCanvas(block: Canvas.() -> Unit) {
    canvas.save()
    canvas.block()
    canvas.restore()
  }

  /** Creates [TextComponent]s. It’s recommended to use this via [TextComponent.build]. */
  public class Builder {
    /** @see [TextComponent.color] */
    public var color: Int = Color.BLACK

    /** @see [TextComponent.textSizeSp] */
    public var textSizeSp: Float = TEXT_COMPONENT_TEXT_SIZE

    /** @see [TextComponent.typeface] */
    public var typeface: Typeface? = null

    /** @see [TextComponent.ellipsize] */
    public var ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END

    /** @see [TextComponent.lineCount] */
    public var lineCount: Int = LABEL_LINE_COUNT

    /** @see [TextComponent.background] */
    public var background: Component? = null

    /** @see TextComponent.textAlignment */
    public var textAlignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL

    /** Defines the minimum width. */
    public var minWidth: MinWidth = MinWidth.fixed()

    /** @see [TextComponent.padding] */
    public var padding: Dimensions = Dimensions.Empty

    /** @see [TextComponent.margins] */
    public var margins: Dimensions = Dimensions.Empty

    /** Creates a new instance of [TextComponent] with the supplied properties. */
    public fun build(): TextComponent =
      TextComponent().apply {
        color = this@Builder.color
        textSizeSp = this@Builder.textSizeSp
        typeface = this@Builder.typeface
        ellipsize = this@Builder.ellipsize
        lineCount = this@Builder.lineCount
        background = this@Builder.background
        textAlignment = this@Builder.textAlignment
        minWidth = this@Builder.minWidth
        padding = this@Builder.padding
        margins = this@Builder.margins
      }
  }

  /** Defines a [TextComponent]’s minimum width. */
  public fun interface MinWidth {
    /** Returns the minimum width. */
    public fun getValue(
      context: MeasureContext,
      textComponent: TextComponent,
      maxWidth: Int,
      maxHeight: Int,
      rotationDegrees: Float,
    ): Float

    /** Houses [MinWidth] factory functions. */
    public companion object {
      internal class Fixed(private val valueDp: Float) : MinWidth {
        override fun getValue(
          context: MeasureContext,
          textComponent: TextComponent,
          maxWidth: Int,
          maxHeight: Int,
          rotationDegrees: Float,
        ) = context.run { valueDp.pixels }

        override fun equals(other: Any?) =
          this === other || other is Fixed && valueDp == other.valueDp

        override fun hashCode() = valueDp.hashCode()
      }

      internal class Text(private val text: CharSequence) : MinWidth {
        private val bounds = RectF()

        override fun getValue(
          context: MeasureContext,
          textComponent: TextComponent,
          maxWidth: Int,
          maxHeight: Int,
          rotationDegrees: Float,
        ) =
          context.run {
            textComponent
              .getLayout(context, text, maxWidth, maxHeight, rotationDegrees)
              .getBounds(bounds)
              .width() + textComponent.padding.horizontalDp.pixels
          }

        override fun equals(other: Any?) = this === other || other is Text && text == other.text

        override fun hashCode() = text.hashCode()
      }

      /** Sets the minimum width to [valueDp] dp. */
      public fun fixed(valueDp: Float = 0f): MinWidth = Fixed(valueDp)

      /** Sets the minimum width to the intrinsic width of the [TextComponent] for [text]. */
      public fun text(text: CharSequence): MinWidth = Text(text)
    }
  }

  /** Houses a [TextComponent] factory function. */
  public companion object {
    private val cacheKeyNamespace = CacheStore.KeyNamespace()

    /**
     * Creates a [TextComponent] via [Builder]. Sample usage:
     * ```
     * TextComponent.build {
     *     color = Color.BLACK
     *     textSizeSp = 12f
     *     typeface = Typeface.MONOSPACE
     * }
     * ```
     */
    public inline fun build(block: Builder.() -> Unit = {}): TextComponent =
      Builder().apply(block).build()
  }
}

private val fm: Paint.FontMetrics = Paint.FontMetrics()

internal val Paint.lineHeight: Float
  get() {
    getFontMetrics(fm)
    return fm.bottom - fm.top + fm.leading
  }
