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

package com.patrykandpatrick.vico.multiplatform.cartesian.axis

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.common.MeasuringContext
import com.patrykandpatrick.vico.multiplatform.common.component.LineComponent
import com.patrykandpatrick.vico.multiplatform.common.component.TextComponent
import com.patrykandpatrick.vico.multiplatform.common.orZero
import com.patrykandpatrick.vico.multiplatform.common.setAll

/**
 * A base [Axis] implementation. This is extended by [HorizontalAxis] and [VerticalAxis].
 *
 * @property line used for the axis line.
 * @property label used for the labels.
 * @property labelRotationDegrees the label rotation (in degrees).
 * @property valueFormatter formats the values.
 * @property tick used for the ticks.
 * @property tickLength the tick length.
 * @property guideline used for the guidelines.
 * @property size determines how the [BaseAxis] sizes itself.
 * @property titleComponent the title [TextComponent].
 * @property title the title text.
 */
public abstract class BaseAxis<P : Axis.Position>(
  protected val line: LineComponent?,
  protected val label: TextComponent?,
  protected val labelRotationDegrees: Float,
  protected val valueFormatter: CartesianValueFormatter,
  protected val tick: LineComponent?,
  protected val tickLength: Dp,
  protected val guideline: LineComponent?,
  protected val size: Size,
  protected val titleComponent: TextComponent?,
  protected val title: CharSequence?,
) : Axis<P> {
  private val restrictedBounds: MutableList<Rect> = mutableListOf()

  override var bounds: Rect = Rect.Zero

  protected val MeasuringContext.lineThickness: Float
    get() = line?.thickness?.pixels.orZero

  protected val MeasuringContext.tickThickness: Float
    get() = tick?.thickness?.pixels.orZero

  protected val MeasuringContext.guidelineThickness: Float
    get() = guideline?.thickness?.pixels.orZero

  protected val MeasuringContext.tickLength: Float
    get() = if (tick != null) this@BaseAxis.tickLength.pixels else 0f

  override fun setRestrictedBounds(vararg bounds: Rect?) {
    restrictedBounds.setAll(bounds.filterNotNull())
  }

  protected fun isNotInRestrictedBounds(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ): Boolean = restrictedBounds.none { it.overlaps(Rect(left, top, right, bottom)) }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is BaseAxis<*> &&
        position == other.position &&
        line == other.line &&
        label == other.label &&
        labelRotationDegrees == other.labelRotationDegrees &&
        valueFormatter == other.valueFormatter &&
        tick == other.tick &&
        tickLength == other.tickLength &&
        guideline == other.guideline &&
        size == other.size &&
        titleComponent == other.titleComponent &&
        title == other.title

  override fun hashCode(): Int {
    var result = line.hashCode()
    result = 31 * result + position.hashCode()
    result = 31 * result + label.hashCode()
    result = 31 * result + labelRotationDegrees.hashCode()
    result = 31 * result + valueFormatter.hashCode()
    result = 31 * result + tick.hashCode()
    result = 31 * result + tickLength.hashCode()
    result = 31 * result + guideline.hashCode()
    result = 31 * result + size.hashCode()
    result = 31 * result + titleComponent.hashCode()
    result = 31 * result + title.hashCode()
    return result
  }

  /**
   * Determines how a [BaseAxis] sizes itself.
   * - For [VerticalAxis], this defines the width.
   * - For [HorizontalAxis], this defines the height.
   */
  @Immutable
  public sealed class Size {
    /**
     * Allows a [BaseAxis] to adopt its preferred size from the given range.
     *
     * @property min the minimum size.
     * @property max the maximum size.
     */
    public class Auto(public val min: Dp = 0.dp, public val max: Dp = Dp.Infinity) : Size() {
      override fun equals(other: Any?): Boolean =
        this === other || other is Auto && min == other.min && max == other.max

      override fun hashCode(): Int = 31 * min.hashCode() + max.hashCode()
    }

    /**
     * Instructs a [BaseAxis] to adopt the given size.
     *
     * @property value the size.
     */
    public class Fixed(public val value: Dp) : Size() {
      override fun equals(other: Any?): Boolean =
        this === other || other is Fixed && value == other.value

      override fun hashCode(): Int = value.hashCode()
    }

    /**
     * The axis will use a fraction of the available space.
     *
     * @property fraction the fraction of the available space that the axis should use.
     */
    public class Fraction(public val fraction: Float) : Size() {
      init {
        require(fraction in MIN..MAX) {
          "Expected a value in the interval [$MIN, $MAX]. Got $fraction."
        }
      }

      override fun equals(other: Any?): Boolean =
        this === other || other is Fraction && fraction == other.fraction

      override fun hashCode(): Int = fraction.hashCode()

      private companion object {
        const val MIN = 0f
        const val MAX = 0.5f
      }
    }

    /**
     * The axis will measure the size of its label component ([label]) for the given [String]
     * ([text]), and it will use this size. In the case of [VerticalAxis], the width of the axis
     * line and the tick length will also be considered.
     */
    public class Text(public val text: CharSequence) : Size() {
      override fun equals(other: Any?): Boolean =
        this === other || other is Text && text == other.text

      override fun hashCode(): Int = text.hashCode()
    }
  }
}
