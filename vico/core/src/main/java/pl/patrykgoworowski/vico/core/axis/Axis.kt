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

package pl.patrykgoworowski.vico.core.axis

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import pl.patrykgoworowski.vico.core.axis.formatter.DefaultAxisFormatter
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.context.MeasureContext
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.extension.setAll

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

    public val MeasureContext.tickLength: Float
        get() = if (tick != null) tickLengthDp.pixels else 0f

    public var isLtr: Boolean = true

    public var label: TextComponent? = null
    public var axisLine: LineComponent? = null
    public var tick: LineComponent? = null
    public var guideline: LineComponent? = null
    public var tickLengthDp: Float = 0f

    public var sizeConstraint: SizeConstraint = SizeConstraint.Auto()

    public var valueFormatter: AxisValueFormatter = DefaultAxisFormatter

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

    public open class Builder(builder: Builder? = null) {
        public var label: TextComponent? = builder?.label

        public var axis: LineComponent? = builder?.axis

        public var tick: LineComponent? = builder?.tick

        public var tickLengthDp: Float = builder?.tickLengthDp ?: Dimens.AXIS_TICK_LENGTH

        public var guideline: LineComponent? = builder?.guideline

        public var valueFormatter: AxisValueFormatter =
            builder?.valueFormatter ?: DecimalFormatAxisValueFormatter()

        public var sizeConstraint: SizeConstraint = SizeConstraint.Auto()
    }

    /**
     * Size constraint of [Axis]:
     * - in [pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis] defines width.
     * - in [pl.patrykgoworowski.vico.core.axis.horizontal.HorizontalAxis] defines height.
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

public fun axisBuilder(block: Axis.Builder.() -> Unit = {}): Axis.Builder =
    Axis.Builder().apply(block)

public fun <T : AxisPosition, A : Axis<T>> Axis.Builder.setTo(axis: A): A {
    axis.axisLine = this.axis
    axis.tick = tick
    axis.guideline = guideline
    axis.label = label
    axis.tickLengthDp = tickLengthDp
    axis.valueFormatter = valueFormatter
    axis.sizeConstraint = sizeConstraint
    return axis
}
