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

package pl.patrykgoworowski.vico.core.dataset.layout

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.axis.AxisManager
import pl.patrykgoworowski.vico.core.axis.model.DataSetModel
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.renderer.DataSet
import pl.patrykgoworowski.vico.core.dimensions.DataSetInsetter
import pl.patrykgoworowski.vico.core.dimensions.Dimensions
import pl.patrykgoworowski.vico.core.dimensions.MutableDimensions
import pl.patrykgoworowski.vico.core.dimensions.floatDimensions
import kotlin.math.max

public open class VirtualLayout(
    var isLTR: Boolean
) {

    private val tempInsetters = ArrayList<DataSetInsetter>(5)
    private val finalInsets: MutableDimensions = floatDimensions()
    private val tempInsets: MutableDimensions = floatDimensions()

    public open fun <Model : EntryModel> setBounds(
        contentBounds: RectF,
        dataSet: DataSet<Model>,
        model: Model,
        dataSetModel: DataSetModel,
        axisManager: AxisManager,
        vararg dataSetInsetter: DataSetInsetter?,
    ) {
        tempInsetters.clear()
        finalInsets.clear()
        tempInsets.clear()
        axisManager.isLTR = isLTR
        axisManager.addInsetters(tempInsetters)
        dataSetInsetter.forEach { it?.let(tempInsetters::add) }

        tempInsetters.forEach { insetter ->
            insetter.getVerticalInsets(tempInsets, model, dataSetModel)
            finalInsets.setAllGreater(tempInsets)
        }

        val availableHeight = contentBounds.height() - finalInsets.vertical

        tempInsetters.forEach { insetter ->
            insetter.getHorizontalInsets(tempInsets, availableHeight, model, dataSetModel)
            finalInsets.setAllGreater(tempInsets)
        }

        dataSet.setBounds(
            left = contentBounds.left + finalInsets.getLeft(isLTR),
            top = contentBounds.top + finalInsets.top,
            right = contentBounds.right - finalInsets.getRight(isLTR),
            bottom = contentBounds.bottom - finalInsets.bottom
        )
        axisManager.setAxesBounds(contentBounds, dataSet.bounds, finalInsets)
    }

    private fun MutableDimensions.setAllGreater(other: Dimensions) {
        start = max(start, other.start)
        top = max(top, other.top)
        end = max(end, other.end)
        bottom = max(bottom, other.bottom)
    }

}