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

package pl.patrykgoworowski.vico.core.axis

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.DefaultDimens
import pl.patrykgoworowski.vico.core.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import pl.patrykgoworowski.vico.core.axis.formatter.DefaultAxisFormatter
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.context.MeasureContext
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.extension.setAll

/**
 * A basic implementation of [AxisRenderer] used throughout the library.
 *
 * @see AxisRenderer
 * @see pl.patrykgoworowski.vico.core.axis.horizontal.HorizontalAxis
 * @see pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis
 */
public abstract class Axis<Position : AxisPosition> : AxisRenderer<Position> {

    private val restrictedBounds: MutableList<RectF> = mutableListOf()

    protected val labels: ArrayList<String> = ArrayList()

    override val bounds: RectF = RectF()

    protected val MeasureContext.axisThickness: Float
        get() = axisLine?.thicknessDp.orZero.pixels

    protected val MeasureContext.tickThickness: Float
        get() = tick?.thicknessDp.orZero.pixels

    protected val MeasureContext.guidelineThickness: Float
        get() = guideline?.thicknessDp.orZero.pixels

    protected val MeasureContext.tickLength: Float
        get() = if (tick != null) tickLengthDp.pixels else 0f

    /**
     * Whether the chart is drawn in the left-to-right layout system.
     */
    public var isLtr: Boolean = true

    /**
     * The [TextComponent] to use for labels.
     */
    public var label: TextComponent? = null

    /**
     * The [LineComponent] to use for axis lines.
     */
    public var axisLine: LineComponent? = null

    /**
     * The [LineComponent] to use for ticks.
     */
    public var tick: LineComponent? = null

    /**
     * The [LineComponent] to use for guidelines.
     */
    public var guideline: LineComponent? = null

    /**
     * The tick length in dp.
     */
    public var tickLengthDp: Float = 0f

    /**
     * The [SizeConstraint] used by [Axis] subclasses to lay themselves out.
     */
    public var sizeConstraint: SizeConstraint = SizeConstraint.Auto()

    /**
     * The [AxisValueFormatter] for the axis.
     */
    public var valueFormatter: AxisValueFormatter<Position> = DefaultAxisFormatter()

    override fun setRestrictedBounds(vararg bounds: RectF?) {
        restrictedBounds.setAll(bounds.filterNotNull())
    }

    protected fun isNotInRestrictedBounds(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Boolean = restrictedBounds.none {
        it.contains(left, top, right, bottom) || it.intersects(left, top, right, bottom)
    }

    /**
     * The base builder class for constructing [Axis] instances.
     */
    public open class Builder<Position : AxisPosition>(builder: Builder<Position>? = null) {
        /**
         * The [TextComponent] to use for labels.
         */
        public var label: TextComponent? = builder?.label

        /**
         * The [LineComponent] to use for the axis.
         */
        public var axis: LineComponent? = builder?.axis

        /**
         * The [LineComponent] to use for axis ticks.
         */
        public var tick: LineComponent? = builder?.tick

        /**
         * The tick length in dp.
         */
        public var tickLengthDp: Float = builder?.tickLengthDp ?: DefaultDimens.AXIS_TICK_LENGTH

        /**
         * The [LineComponent] to use for guidelines.
         */
        public var guideline: LineComponent? = builder?.guideline

        /**
         * The [AxisValueFormatter] for the axis.
         */
        public var valueFormatter: AxisValueFormatter<Position> =
            builder?.valueFormatter ?: DecimalFormatAxisValueFormatter()

        /**
         * The [SizeConstraint] used by [Axis] subclasses to lay themselves out.
         */
        public var sizeConstraint: SizeConstraint = SizeConstraint.Auto()
    }

    /**
     * The size constraint of an [Axis].
     * - In [pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis], this defines the width.
     * - In [pl.patrykgoworowski.vico.core.axis.horizontal.HorizontalAxis], this defines the height.
     *
     * @see [pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis]
     * @see [pl.patrykgoworowski.vico.core.axis.horizontal.HorizontalAxis]
     */
    public sealed class SizeConstraint {

        /**
         * The axis will measure itself and use as much space as it needs, but not less than [minSizeDp] and no more
         * than [maxSizeDp].
         */
        public class Auto(
            public val minSizeDp: Float = 0f,
            public val maxSizeDp: Float = Float.MAX_VALUE,
        ) : SizeConstraint()

        /**
         * The axis size will be exactly [sizeDp].
         */
        public class Exact(public val sizeDp: Float) : SizeConstraint()

        /**
         * The axis size will take an exact fraction of available size:
         * - in [pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis] the width.
         * - in [pl.patrykgoworowski.vico.core.axis.horizontal.HorizontalAxis] the height.
         * @property fraction the fraction of the available space that the axis should use.
         */
        public class Fraction(public val fraction: Float) : SizeConstraint() {
            init {
                if (fraction !in MIN..MAX) {
                    throw IllegalArgumentException("Expected a value in range of $MIN to $MAX. Got $fraction.")
                }
            }

            private companion object {
                const val MIN = 0f
                const val MAX = 0.5f
            }
        }

        /**
         * The axis will measure actual width of given [text] and use it as its size.
         * [pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis] will append the width of axis line,
         * the tick length, as well as [TextComponent] horizontal padding and margins.
         */
        public class TextWidth(public val text: String) : SizeConstraint()
    }
}

/**
 * Provides a quick way to create an axis. Creates an [Axis.Builder] instance, calls the provided function block with
 * the [Axis.Builder] instance as its receiver, and returns the [Axis.Builder] instance.
 */
public fun <Position : AxisPosition> axisBuilder(
    block: Axis.Builder<Position>.() -> Unit = {},
): Axis.Builder<Position> = Axis.Builder<Position>().apply(block)

/**
 * A convenience function that allows for applying the properties from an [Axis.Builder] to an [Axis] subclass.
 *
 * @param axis the [Axis] whose properties will be updated to this [Axis.Builder]’s properties.
 */
public fun <Position : AxisPosition, A : Axis<Position>> Axis.Builder<Position>.setTo(axis: A): A {
    axis.axisLine = this.axis
    axis.tick = tick
    axis.guideline = guideline
    axis.label = label
    axis.tickLengthDp = tickLengthDp
    axis.valueFormatter = valueFormatter
    axis.sizeConstraint = sizeConstraint
    return axis
}
