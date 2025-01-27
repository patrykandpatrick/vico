/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.multiplatform.common.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.common.Defaults.TEXT_COMPONENT_LINE_COUNT
import com.patrykandpatrick.vico.multiplatform.common.DrawingContext
import com.patrykandpatrick.vico.multiplatform.common.Insets
import com.patrykandpatrick.vico.multiplatform.common.MeasuringContext
import com.patrykandpatrick.vico.multiplatform.common.Position
import com.patrykandpatrick.vico.multiplatform.common.bounds
import com.patrykandpatrick.vico.multiplatform.common.component.TextComponent.MinWidth.Companion.text
import com.patrykandpatrick.vico.multiplatform.common.data.CacheStore
import com.patrykandpatrick.vico.multiplatform.common.extendBy
import com.patrykandpatrick.vico.multiplatform.common.half
import com.patrykandpatrick.vico.multiplatform.common.measure
import com.patrykandpatrick.vico.multiplatform.common.piRad
import com.patrykandpatrick.vico.multiplatform.common.rotate
import com.patrykandpatrick.vico.multiplatform.common.toRadians
import kotlin.jvm.JvmName
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val DEF_LAYOUT_SIZE = 10000

/**
 * Uses [Canvas] to render text. This class utilizes [TextMeasurer] and supports the following:
 * - multi-line text with automatic line breaking
 * - text truncation
 * - [AnnotatedString]
 * - text rotation
 * - text backgrounds (any [Component])
 * - margins and padding
 *
 * @property textStyle the text style.
 * @property lineCount the line count.
 * @property textOverflow the truncation type.
 * @property padding the padding.
 * @property margins the margins.
 * @property background drawn behind the text.
 * @property minWidth defines the minimum width.
 */
@Immutable
public open class TextComponent(
  protected val textStyle: TextStyle = TextStyle.Default,
  protected val lineCount: Int = TEXT_COMPONENT_LINE_COUNT,
  protected val textOverflow: TextOverflow = TextOverflow.Ellipsis,
  protected val margins: Insets = Insets.Zero,
  protected val padding: Insets = Insets.Zero,
  public val background: Component? = null,
  protected val minWidth: MinWidth = MinWidth.fixed(),
) {
  private lateinit var textLayoutResult: TextLayoutResult
  private lateinit var measuringLayoutResult: TextLayoutResult

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
      textLayoutResult = getTextLayoutResult(this, text, maxWidth, maxHeight, rotationDegrees)

      val shouldRotate = rotationDegrees % 2f.piRad != 0f
      val textStartPosition =
        horizontalPosition.getTextStartPosition(context, x, textLayoutResult.size.width.toFloat())
      val textTopPosition =
        verticalPosition.getTextTopPosition(context, y, textLayoutResult.size.height.toFloat())

      context.withSavedCanvas {
        var textBounds = textLayoutResult.bounds
        val paddingLeft = padding.getLeft(context)
        val paddingRight = padding.getRight(context)
        val textAlignmentCorrection: Float

        textBounds =
          textBounds.run {
            val minWidth =
              minWidth.getValue(context, this@TextComponent, maxWidth, maxHeight, rotationDegrees) -
                padding.horizontal.pixels
            val minWidthCorrection =
              (minWidth.coerceAtMost(textLayoutResult.size.width.toFloat()) - width).coerceAtLeast(
                0f
              )
            textAlignmentCorrection = getTextAlignmentCorrection(width)
            Rect(
              (left - minWidthCorrection.half - paddingLeft),
              top - padding.top.pixels,
              right + minWidthCorrection.half + paddingRight,
              bottom + padding.bottom.pixels,
            )
          }

        var xCorrection = 0f
        var yCorrection = 0f

        if (shouldRotate) {
          val boundsPostRotation = textBounds.rotate(rotationDegrees)
          val heightDelta = textBounds.height - boundsPostRotation.height
          val widthDelta = textBounds.width - boundsPostRotation.width

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
        textBounds =
          textBounds.translate(textStartPosition + xCorrection, textTopPosition + yCorrection)

        if (shouldRotate) rotate(rotationDegrees, textBounds.center.x, textBounds.center.y)

        background?.draw(
          context = context,
          left = textBounds.left,
          top = textBounds.top,
          right = textBounds.right,
          bottom = textBounds.bottom,
        )

        translate(
          (textBounds.left + paddingLeft + textAlignmentCorrection),
          (textBounds.top + padding.top.pixels),
        )

        textLayoutResult.multiParagraph.paint(canvas)
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
    val textAlignment = textStyle.textAlign
    val ltrAlignment =
      if (textLayoutResult.getParagraphDirection(0) == ResolvedTextDirection.Ltr) {
        textAlignment
      } else {
        when (textAlignment) {
          TextAlign.Left -> TextAlign.Right
          TextAlign.Right -> TextAlign.Left
          else -> textAlignment
        }
      }
    return when (ltrAlignment) {
      TextAlign.End,
      TextAlign.Right -> width - textLayoutResult.size.width
      TextAlign.Center -> (width - textLayoutResult.size.width).half
      else -> 0f
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
          Position.Vertical.Top -> -layoutHeight - padding.bottom.pixels - margins.bottom.pixels
          Position.Vertical.Center -> -layoutHeight.half
          Position.Vertical.Bottom -> padding.top.pixels + margins.top.pixels
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
      .width

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
      .height

  /**
   * Returns the bounds ([Rect]) of this [TextComponent] for the given [text] and maximum
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
  ): Rect =
    with(context) {
      var measuredText = text ?: ""
      if (pad) {
        measuredText = buildAnnotatedString {
          append(text)
          repeat((lineCount - measuredText.lines().size).coerceAtLeast(0)) { append('\n') }
        }
      }
      val layout = getTextLayoutResult(this, measuredText, maxWidth, maxHeight, rotationDegrees)
      layout
        .run {
          val minWidth =
            minWidth.getValue(context, this@TextComponent, maxWidth, maxHeight, rotationDegrees) -
              padding.horizontal.pixels
          Rect(
            left = 0f,
            top = 0f,
            right =
              size.width
                .toFloat()
                .coerceAtLeast(minWidth)
                .coerceAtMost(layout.size.width.toFloat()) + padding.horizontal.pixels,
            bottom = size.height.toFloat() + padding.vertical.pixels,
          )
        }
        .rotate(rotationDegrees)
        .extendBy(right = margins.horizontal.pixels, bottom = margins.vertical.pixels)
    }

  /** Creates a new [TextComponent] based on this one. */
  public open fun copy(
    style: TextStyle = this.textStyle,
    lineCount: Int = this.lineCount,
    textOverflow: TextOverflow = this.textOverflow,
    margins: Insets = this.margins,
    padding: Insets = this.padding,
    background: Component? = this.background,
    minWidth: MinWidth = this.minWidth,
  ): TextComponent =
    TextComponent(style, lineCount, textOverflow, margins, padding, background, minWidth)

  private fun getTextLayoutResult(
    context: MeasuringContext,
    text: CharSequence,
    width: Int = Constraints.Infinity,
    height: Int = Constraints.Infinity,
    rotationDegrees: Float = 0f,
  ) =
    context.run {
      val measurer =
        cacheStore.getOrSet(cacheKeyNamespace, fontFamilyResolver, density, layoutDirection) {
          TextMeasurer(
            defaultFontFamilyResolver = fontFamilyResolver,
            defaultDensity = density,
            defaultLayoutDirection = layoutDirection,
            cacheSize = 0,
          )
        }

      measuringLayoutResult =
        measurer.measure(text, textStyle, textOverflow, lineCount, width, height)

      val widthWithoutMargins = width - margins.horizontal.pixels.toInt()
      val heightWithoutMargins = height - margins.vertical.pixels.toInt()

      val correctedWidth =
        (when {
            rotationDegrees % 1f.piRad == 0f -> widthWithoutMargins
            rotationDegrees % 0.5f.piRad == 0f -> heightWithoutMargins
            else -> {
              val cumulatedHeight =
                lineCount * measuringLayoutResult.size.height + padding.vertical.pixels.toInt()
              val alpha = rotationDegrees.toRadians()
              val absSinAlpha = sin(alpha).absoluteValue
              val absCosAlpha = cos(alpha).absoluteValue
              val basedOnWidth = (widthWithoutMargins - cumulatedHeight * absSinAlpha) / absCosAlpha
              val basedOnHeight =
                (heightWithoutMargins - cumulatedHeight * absCosAlpha) / absSinAlpha
              min(basedOnWidth, basedOnHeight).toInt()
            }
          } - padding.horizontal.pixels.toInt())
          .coerceAtLeast(0)

      cacheStore.getOrSet(
        cacheKeyNamespace,
        text,
        textStyle,
        textOverflow,
        lineCount,
        correctedWidth,
        height,
      ) {
        measurer.measure(text, textStyle, textOverflow, lineCount, correctedWidth, height)
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
        textStyle == other.textStyle &&
        lineCount == other.lineCount &&
        textOverflow == other.textOverflow &&
        margins == other.margins &&
        padding == other.padding &&
        background == other.background &&
        minWidth == other.minWidth

  override fun hashCode(): Int {
    var result = textStyle.hashCode()
    result = 31 * result + lineCount
    result = 31 * result + textOverflow.hashCode()
    result = 31 * result + margins.hashCode()
    result = 31 * result + padding.hashCode()
    result = 31 * result + background.hashCode()
    result = 31 * result + minWidth.hashCode()
    return result
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
      internal class Fixed(private val value: Dp) : MinWidth {
        override fun getValue(
          context: MeasuringContext,
          textComponent: TextComponent,
          maxWidth: Int,
          maxHeight: Int,
          rotationDegrees: Float,
        ) = context.run { value.pixels }

        override fun equals(other: Any?) = this === other || other is Fixed && value == other.value

        override fun hashCode() = value.hashCode()
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
              .getTextLayoutResult(context, text, maxWidth, maxHeight, rotationDegrees)
              .size
              .width + textComponent.padding.horizontal.pixels
          }

        override fun equals(other: Any?) = this === other || other is Text && text == other.text

        override fun hashCode() = text.hashCode()
      }

      /** Sets the minimum width to [value] dp. */
      public fun fixed(value: Dp = 0f.dp): MinWidth = Fixed(value)

      /** Sets the minimum width to the intrinsic width of the [TextComponent] for [text]. */
      public fun text(text: CharSequence): MinWidth = Text(text)
    }
  }

  protected companion object {
    public val cacheKeyNamespace: CacheStore.KeyNamespace = CacheStore.KeyNamespace()
  }
}
