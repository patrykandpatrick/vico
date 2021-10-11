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

package pl.patrykgoworowski.vico.core.layout

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.annotation.LongParameterListDrawFunction
import pl.patrykgoworowski.vico.core.axis.AxisManager
import pl.patrykgoworowski.vico.core.axis.model.DataSetModel
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.renderer.DataSet
import pl.patrykgoworowski.vico.core.dataset.insets.DataSetInsetter
import pl.patrykgoworowski.vico.core.dataset.insets.Insets
import kotlin.math.max

public open class VirtualLayout {

    private val tempInsetters = ArrayList<DataSetInsetter>(TEMP_INSETTERS_INITIAL_SIZE)
    private val finalInsets: Insets = Insets()
    private val tempInsets: Insets = Insets()

    @LongParameterListDrawFunction
    public open fun <Model : EntryModel> setBounds(
        context: MeasureContext,
        contentBounds: RectF,
        dataSet: DataSet<Model>,
        dataSetModel: DataSetModel,
        axisManager: AxisManager,
        vararg dataSetInsetter: DataSetInsetter?,
    ) = with(context) {
        tempInsetters.clear()
        finalInsets.clear()
        tempInsets.clear()
        axisManager.addInsetters(tempInsetters)
        dataSetInsetter.filterNotNull().forEach(tempInsetters::add)

        tempInsetters.forEach { insetter ->
            insetter.getVerticalInsets(context, dataSetModel, tempInsets)
            finalInsets.setAllGreater(tempInsets)
        }

        val availableHeight = contentBounds.height() - finalInsets.vertical

        tempInsetters.forEach { insetter ->
            insetter.getHorizontalInsets(context, availableHeight, dataSetModel, tempInsets)
            finalInsets.setAllGreater(tempInsets)
        }

        dataSet.setBounds(
            left = contentBounds.left + finalInsets.getLeft(isLtr),
            top = contentBounds.top + finalInsets.top,
            right = contentBounds.right - finalInsets.getRight(isLtr),
            bottom = contentBounds.bottom - finalInsets.bottom
        )
        axisManager.setAxesBounds(context, contentBounds, dataSet.bounds, finalInsets)
    }

    private fun Insets.setAllGreater(other: Insets) {
        start = max(start, other.start)
        top = max(top, other.top)
        end = max(end, other.end)
        bottom = max(bottom, other.bottom)
    }

    companion object {
        private const val TEMP_INSETTERS_INITIAL_SIZE = 5
    }
}
