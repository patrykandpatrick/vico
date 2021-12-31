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
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.extension.set
import pl.patrykgoworowski.vico.core.extension.setAll
import pl.patrykgoworowski.vico.core.context.MeasureContext

public abstract class Axis<Position : AxisPosition> : AxisRenderer<Position> {

    protected val labels: ArrayList<String> = ArrayList()

    override val bounds: RectF = RectF()
    override val restrictedBounds: MutableList<RectF> = mutableListOf()
    override val chartBounds: RectF = RectF()

    override val MeasureContext.axisThickness: Float
        get() = axis?.thicknessDp.orZero.pixels

    override val MeasureContext.tickThickness: Float
        get() = tick?.thicknessDp.orZero.pixels

    override val MeasureContext.guidelineThickness: Float
        get() = guideline?.thicknessDp.orZero.pixels

    override val MeasureContext.tickLength: Float
        get() = if (tick != null) tickLengthDp.pixels else 0f

    override var isLtr: Boolean = true

    override var label: TextComponent? = null
    override var axis: LineComponent? = null
    override var tick: LineComponent? = null
    override var guideline: LineComponent? = null
    override var tickLengthDp: Float = 0f

    override var valueFormatter: AxisValueFormatter = DefaultAxisFormatter

    override fun setChartBounds(left: Number, top: Number, right: Number, bottom: Number) {
        chartBounds.set(left, top, right, bottom)
    }

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

        public var tickLengthDp: Float =
            builder?.tickLengthDp ?: Dimens.AXIS_TICK_LENGTH

        public var guideline: LineComponent? = builder?.guideline

        public var valueFormatter: AxisValueFormatter =
            builder?.valueFormatter ?: DecimalFormatAxisValueFormatter()
    }
}

public fun axisBuilder(block: Axis.Builder.() -> Unit = {}): Axis.Builder =
    Axis.Builder().apply(block)

public fun <T : AxisPosition, A : Axis<T>> Axis.Builder.setTo(axis: A): A {
    axis.axis = this.axis
    axis.tick = tick
    axis.guideline = guideline
    axis.label = label
    axis.tickLengthDp = tickLengthDp
    axis.valueFormatter = valueFormatter
    return axis
}
