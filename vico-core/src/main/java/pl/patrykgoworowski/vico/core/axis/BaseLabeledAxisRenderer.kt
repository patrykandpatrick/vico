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
import pl.patrykgoworowski.vico.core.axis.component.TickComponent
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.extension.set
import pl.patrykgoworowski.vico.core.extension.setAll
import kotlin.properties.Delegates

public abstract class BaseLabeledAxisRenderer<Position : AxisPosition>(
    override var label: TextComponent?,
    override var axis: LineComponent?,
    override var tick: TickComponent?,
    override var guideline: LineComponent?,
) : AxisRenderer<Position> {

    protected val labels = ArrayList<String>()

    override val bounds: RectF = RectF()
    override val restrictedBounds: MutableList<RectF> = mutableListOf()
    override val dataSetBounds: RectF = RectF()

    override val axisThickness: Float
        get() = axis?.thickness.orZero

    override val tickLength: Float
        get() = tick?.length.orZero

    override val tickThickness: Float
        get() = tick?.thickness.orZero

    override val guidelineThickness: Float
        get() = guideline?.thickness.orZero

    override var isLTR: Boolean by Delegates.observable(true) { _, _, value ->
        label?.isLTR = value
    }

    override fun setDataSetBounds(left: Number, top: Number, right: Number, bottom: Number) {
        dataSetBounds.set(left, top, right, bottom)
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
}
