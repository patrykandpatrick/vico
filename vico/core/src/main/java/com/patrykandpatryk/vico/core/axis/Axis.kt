/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatryk.vico.core.axis

import android.graphics.RectF
import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatryk.vico.core.axis.formatter.DefaultAxisValueFormatter
import com.patrykandpatryk.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatryk.vico.core.component.shape.LineComponent
import com.patrykandpatryk.vico.core.component.text.TextComponent
import com.patrykandpatryk.vico.core.context.MeasureContext
import com.patrykandpatryk.vico.core.extension.orZero
import com.patrykandpatryk.vico.core.extension.setAll

/**
 * A basic implementation of [AxisRenderer] used throughout the library.
 *
 * @see AxisRenderer
 * @see HorizontalAxis
 * @see VerticalAxis
 */
public abstract class Axis<Position : AxisPosition> : AxisRenderer<Position> {

    private val restrictedBounds: MutableList<RectF> = mutableListOf()

    protected val labels: ArrayList<CharSequence> = ArrayList()

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
     * The [TextComponent] to use for labels.
     */
    public var label: TextComponent? = null

    /**
     * The [LineComponent] to use for the axis line.
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
     * The tick length (in dp).
     */
    public var tickLengthDp: Float = 0f

    /**
     * Used by [Axis] subclasses for sizing and layout.
     */
    public var sizeConstraint: SizeConstraint = SizeConstraint.Auto()

    /**
     * The [AxisValueFormatter] for the axis.
     */
    public var valueFormatter: AxisValueFormatter<Position> = DefaultAxisValueFormatter()

    /**
     * The rotation of axis labels (in degrees).
     */
    public var labelRotationDegrees: Float = 0f

    /**
     * An optional [TextComponent] to use as the axis title.
     */
    public var titleComponent: TextComponent? = null

    /**
     * The axis title.
     */
    public var title: CharSequence? = null

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
     * Used to construct [Axis] instances.
     */
    public open class Builder<Position : AxisPosition>(builder: Builder<Position>? = null) {
        /**
         * The [TextComponent] to use for labels.
         */
        public var label: TextComponent? = builder?.label

        /**
         * The [LineComponent] to use for the axis line.
         */
        public var axis: LineComponent? = builder?.axis

        /**
         * The [LineComponent] to use for axis ticks.
         */
        public var tick: LineComponent? = builder?.tick

        /**
         * The tick length (in dp).
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
         * Used by [Axis] subclasses for sizing and layout.
         */
        public var sizeConstraint: SizeConstraint = SizeConstraint.Auto()

        /**
         * An optional [TextComponent] to use as the axis title.
         */
        public var titleComponent: TextComponent? = builder?.titleComponent

        /**
         * The axis title.
         */
        public var title: CharSequence? = builder?.title

        /**
         * The rotation of axis labels (in degrees).
         */
        public var labelRotationDegrees: Float = builder?.labelRotationDegrees ?: 0f
    }

    /**
     * Defines how an [Axis] is to size itself.
     * - For [VerticalAxis], this defines the width.
     * - For [HorizontalAxis], this defines the height.
     *
     * @see [VerticalAxis]
     * @see [HorizontalAxis]
     */
    public sealed class SizeConstraint {

        /**
         * The axis will measure itself and use as much space as it needs, but no less than [minSizeDp], and no more
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
         * The axis will use a fraction of the available space.
         *
         * @property fraction the fraction of the available space that the axis should use.
         */
        public class Fraction(public val fraction: Float) : SizeConstraint() {
            init {
                if (fraction !in MIN..MAX) {
                    throw IllegalArgumentException("Expected a value in the interval [$MIN, $MAX]. Got $fraction.")
                }
            }

            private companion object {
                const val MIN = 0f
                const val MAX = 0.5f
            }
        }

        /**
         * The axis will measure the width of its label component ([label]) for the given [String] ([text]), and it will
         * use this width as its size. In the case of [VerticalAxis], the width of the axis line and the tick length
         * will also be considered.
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
    axis.titleComponent = titleComponent
    axis.title = title
    axis.labelRotationDegrees = labelRotationDegrees
    return axis
}
