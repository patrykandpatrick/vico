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
import androidx.compose.runtime.Immutable
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.Defaults.TEXT_COMPONENT_LINE_COUNT
import com.patrykandpatrick.vico.core.common.DrawingContext
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.MeasuringContext
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.bounds
import com.patrykandpatrick.vico.core.common.copy
import com.patrykandpatrick.vico.core.common.data.CacheStore
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.heightWithSpacingAddition
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
 * @property color the text color.
 * @property typeface the [Typeface].
 * @property textSizeSp the text size (in sp).
 * @property lineHeightSp the line height (in sp).
 * @property textAlignment the text alignment.
 * @property lineCount the line count.
 * @property truncateAt the truncation type.
 * @property padding the padding.
 * @property margins the margins.
 * @property background drawn behind the text.
 * @property minWidth defines the minimum width.
 */
@Immutable
public open class TextComponent(
  protected val color: Int = Color.BLACK,
  protected val typeface: Typeface = Typeface.DEFAULT,
  protected val textSizeSp: Float = Defaults.TEXT_COMPONENT_TEXT_SIZE,
  protected val textAlignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
  protected val lineHeightSp: Float? = null,
  protected val lineCount: Int = TEXT_COMPONENT_LINE_COUNT,
  protected val truncateAt: TextUtils.TruncateAt? = TextUtils.TruncateAt.END,
  protected val margins: Insets = Insets.Zero,
  protected val padding: Insets = Insets.Zero,
  public val background: Component? = null,
  protected val minWidth: MinWidth = MinWidth.fixed(),
) {
  private val textPaint =
    TextPaint(Paint.ANTI_ALIAS_FLAG).also { textPaint ->
      textPaint.color = color
      textPaint.typeface = typeface
      textPaint.textSize = 0f
    }
  private lateinit var layout: Layout
  private lateinit var measuringLayout: Layout

  /**
   * Uses [Canvas] to draw this [TextComponent].
   *
   * @param context holds environment data.
   * @param text the text to be drawn.
   * @param x the _x_ coordinate for the text.
   * @param y the _y_ coordinate for the text.
   * @param horizontalPosition the horizontal position of the text, relative to [x].
   * @param verticalPosition the vertical position of the text, relative to [y].
   * @param maxWidth the maximum width available for the text (in pixels).
   * @param maxHeight the maximum height available for the text (in pixels).
   * @param rotationDegrees the rotation of the text (in degrees).
   */
  public fun draw(
    context: DrawingContext,
    text: CharSequence,
    x: Float,
    y: Float,
    horizontalPosition: Position.Horizontal = Position.Horizontal.Center,
    verticalPosition: Position.Vertical = Position.Vertical.Center,
    maxWidth: Int = DEF_LAYOUT_SIZE,
    maxHeight: Int = DEF_LAYOUT_SIZE,
    rotationDegrees: Float = 0f,
  ) {
    with(context) {
      if (text.isBlank()) return
      layout = getLayout(this, text, maxWidth, maxHeight, rotationDegrees)

      val shouldRotate = rotationDegrees % 2f.piRad != 0f
      val textStartPosition =
        horizontalPosition.getTextStartPosition(context, x, layout.widestLineWidth)
      val textTopPosition =
        verticalPosition.getTextTopPosition(context, y, layout.heightWithSpacingAddition)

      context.withSavedCanvas {
        val bounds = layout.bounds
        val paddingLeft = padding.getLeft(context)
        val paddingRight = padding.getRight(context)
        val textAlignmentCorrection: Float

        with(receiver = bounds) {
          val minWidth =
            minWidth.getValue(context, this@TextComponent, maxWidth, maxHeight, rotationDegrees) -
              padding.horizontalDp.pixels
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
              Position.Horizontal.Start -> widthDelta.half
              Position.Horizontal.End -> -widthDelta.half
              else -> 0f
            } * context.layoutDirectionMultiplier

          yCorrection =
            when (verticalPosition) {
              Position.Vertical.Top -> heightDelta.half
              Position.Vertical.Bottom -> -heightDelta.half
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
          bounds.top + padding.topDp.pixels + layout.spacingAdd.half,
        )

        layout.draw(this)
      }
    }
  }

  private fun Position.Horizontal.getTextStartPosition(
    context: MeasuringContext,
    baseXPosition: Float,
    width: Float,
  ): Float =
    with(context) {
      when (this@getTextStartPosition) {
        Position.Horizontal.Start ->
          if (isLtr) getTextRightPosition(baseXPosition, width)
          else getTextLeftPosition(baseXPosition)
        Position.Horizontal.Center -> baseXPosition - width.half
        Position.Horizontal.End ->
          if (isLtr) getTextLeftPosition(baseXPosition)
          else getTextRightPosition(baseXPosition, width)
      }
    }

  private fun MeasuringContext.getTextLeftPosition(baseXPosition: Float): Float =
    baseXPosition + padding.getLeft(this) + margins.getLeft(this)

  private fun MeasuringContext.getTextRightPosition(baseXPosition: Float, width: Float): Float =
    baseXPosition - padding.getRight(this) - margins.getRight(this) - width

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
  private fun Position.Vertical.getTextTopPosition(
    context: MeasuringContext,
    textY: Float,
    layoutHeight: Float,
  ): Float =
    with(context) {
      textY +
        when (this@getTextTopPosition) {
          Position.Vertical.Top -> -layoutHeight - padding.bottomDp.pixels - margins.bottomDp.pixels
          Position.Vertical.Center -> -layoutHeight.half
          Position.Vertical.Bottom -> padding.topDp.pixels + margins.topDp.pixels
        }
    }

  /**
   * Returns the width of this [TextComponent] for the given text and maximum dimensions. [pad]
   * defines whether to extend [text] by such a number of blank lines that it has [lineCount] lines.
   */
  public fun getWidth(
    context: MeasuringContext,
    text: CharSequence? = null,
    maxWidth: Int = DEF_LAYOUT_SIZE,
    maxHeight: Int = DEF_LAYOUT_SIZE,
    rotationDegrees: Float = 0f,
    pad: Boolean = text == null,
  ): Float =
    getBounds(
        context = context,
        text = text,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        rotationDegrees = rotationDegrees,
        pad = pad,
      )
      .width()

  /**
   * Returns the height of this [TextComponent] for the given text and maximum dimensions. [pad]
   * defines whether to extend [text] by such a number of blank lines that it has [lineCount] lines.
   */
  public fun getHeight(
    context: MeasuringContext,
    text: CharSequence? = null,
    maxWidth: Int = DEF_LAYOUT_SIZE,
    maxHeight: Int = DEF_LAYOUT_SIZE,
    rotationDegrees: Float = 0f,
    pad: Boolean = text == null,
  ): Float =
    getBounds(
        context = context,
        text = text,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        rotationDegrees = rotationDegrees,
        pad = pad,
      )
      .height()

  /**
   * Returns the bounds ([RectF]) of this [TextComponent] for the given [text] and maximum
   * dimensions. [pad] defines whether to extend [text] by such a number of blank lines that it has
   * [lineCount] lines.
   */
  public fun getBounds(
    context: MeasuringContext,
    text: CharSequence? = null,
    maxWidth: Int = DEF_LAYOUT_SIZE,
    maxHeight: Int = DEF_LAYOUT_SIZE,
    rotationDegrees: Float = 0f,
    pad: Boolean = text == null,
  ): RectF =
    with(context) {
      var measuredText = text ?: ""
      if (pad) {
        measuredText = SpannableStringBuilder(measuredText)
        repeat((lineCount - measuredText.lines().size).coerceAtLeast(0)) {
          measuredText.append('\n')
        }
      }
      val layout = getLayout(this, measuredText, maxWidth, maxHeight, rotationDegrees)
      layout.bounds
        .apply {
          val minWidth =
            minWidth.getValue(context, this@TextComponent, maxWidth, maxHeight, rotationDegrees) -
              padding.horizontalDp.pixels
          right = right.coerceAtLeast(minWidth).coerceAtMost(layout.width.toFloat())
          right += padding.horizontalDp.pixels
          bottom += padding.verticalDp.pixels
        }
        .rotate(rotationDegrees)
        .apply {
          right += margins.horizontalDp.pixels
          bottom += margins.verticalDp.pixels
        }
    }

  /** Creates a new [TextComponent] based on this one. */
  public open fun copy(
    color: Int = this.color,
    typeface: Typeface = this.typeface,
    textSizeSp: Float = this.textSizeSp,
    textAlignment: Layout.Alignment = this.textAlignment,
    lineHeightSp: Float? = this.lineHeightSp,
    lineCount: Int = this.lineCount,
    truncateAt: TextUtils.TruncateAt? = this.truncateAt,
    margins: Insets = this.margins,
    padding: Insets = this.padding,
    background: Component? = this.background,
    minWidth: MinWidth = this.minWidth,
  ): TextComponent =
    TextComponent(
      color,
      typeface,
      textSizeSp,
      textAlignment,
      lineHeightSp,
      lineCount,
      truncateAt,
      margins,
      padding,
      background,
      minWidth,
    )

  private fun getLayout(
    context: MeasuringContext,
    text: CharSequence,
    width: Int = DEF_LAYOUT_SIZE,
    height: Int = DEF_LAYOUT_SIZE,
    rotationDegrees: Float = 0f,
  ) =
    context.run {
      textPaint.textSize = spToPx(textSizeSp)
      measuringLayout =
        cacheStore.getOrSet(cacheKeyNamespace, textPaint.typeface.hashCode(), textPaint.textSize) {
          staticLayout("", textPaint, DEF_LAYOUT_SIZE)
        }
      val widthWithoutMargins = width - margins.horizontalDp.wholePixels
      val heightWithoutMargins = height - margins.verticalDp.wholePixels
      val extraLineHeight = getExtraLineHeight()

      val correctedWidth =
        (when {
            rotationDegrees % 1f.piRad == 0f -> widthWithoutMargins
            rotationDegrees % 0.5f.piRad == 0f -> heightWithoutMargins
            else -> {
              val cumulatedHeight =
                lineCount * (measuringLayout.height + extraLineHeight) +
                  padding.verticalDp.wholePixels
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
        textPaint.color,
        textPaint.typeface.hashCode(),
        textPaint.textSize,
        extraLineHeight,
        correctedWidth,
        lineCount,
        truncateAt,
        textAlignment,
      ) {
        staticLayout(
          source = text,
          paint = textPaint,
          spacingAddition = extraLineHeight,
          width = correctedWidth,
          maxLines = lineCount,
          ellipsize = truncateAt,
          align = textAlignment,
        )
      }
    }

  private inline fun DrawingContext.withSavedCanvas(block: Canvas.() -> Unit) {
    canvas.save()
    canvas.block()
    canvas.restore()
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is TextComponent &&
        color == other.color &&
        typeface == other.typeface &&
        textSizeSp == other.textSizeSp &&
        lineHeightSp == other.lineHeightSp &&
        textAlignment == other.textAlignment &&
        lineCount == other.lineCount &&
        truncateAt == other.truncateAt &&
        margins == other.margins &&
        padding == other.padding &&
        background == other.background &&
        minWidth == other.minWidth

  override fun hashCode(): Int {
    var result = color
    result = 31 * result + typeface.hashCode()
    result = 31 * result + textSizeSp.hashCode()
    result = 31 * result + lineHeightSp.hashCode()
    result = 31 * result + textAlignment.hashCode()
    result = 31 * result + lineCount
    result = 31 * result + truncateAt.hashCode()
    result = 31 * result + margins.hashCode()
    result = 31 * result + padding.hashCode()
    result = 31 * result + background.hashCode()
    result = 31 * result + minWidth.hashCode()
    return result
  }

  private fun MeasuringContext.getExtraLineHeight(): Float =
    if (lineHeightSp != null) {
      spToPx(lineHeightSp) -
        measuringLayout.height -
        measuringLayout.topPadding -
        measuringLayout.bottomPadding
    } else {
      0f
    }

  /** Defines a [TextComponent]’s minimum width. */
  @Immutable
  public fun interface MinWidth {
    /** Returns the minimum width. */
    public fun getValue(
      context: MeasuringContext,
      textComponent: TextComponent,
      maxWidth: Int,
      maxHeight: Int,
      rotationDegrees: Float,
    ): Float

    /** Houses [MinWidth] factory functions. */
    public companion object {
      internal class Fixed(private val valueDp: Float) : MinWidth {
        override fun getValue(
          context: MeasuringContext,
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
        override fun getValue(
          context: MeasuringContext,
          textComponent: TextComponent,
          maxWidth: Int,
          maxHeight: Int,
          rotationDegrees: Float,
        ) =
          context.run {
            textComponent
              .getLayout(context, text, maxWidth, maxHeight, rotationDegrees)
              .bounds
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

  protected companion object {
    public val cacheKeyNamespace: CacheStore.KeyNamespace = CacheStore.KeyNamespace()
  }
}
