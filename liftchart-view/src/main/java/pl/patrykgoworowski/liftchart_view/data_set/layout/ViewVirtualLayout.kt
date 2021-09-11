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

package pl.patrykgoworowski.liftchart_view.data_set.layout

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import pl.patrykgoworowski.liftchart_common.data_set.layout.VirtualLayout
import pl.patrykgoworowski.liftchart_common.dimensions.DataSetInsetter
import pl.patrykgoworowski.liftchart_view.data_set.common.DataSetWithModel

public open class ViewVirtualLayout(isLTR: Boolean) : VirtualLayout(isLTR) {

    public open fun <Model : EntryModel> setBounds(
        contentBounds: RectF,
        dataSet: DataSetWithModel<Model>,
        dataSetModel: DataSetModel,
        axisManager: AxisManager,
        vararg dataSetInsetter: DataSetInsetter?,
    ) {
        setBounds(
            contentBounds, dataSet, dataSet.getEntriesModel(), dataSetModel, axisManager,
            *dataSetInsetter
        )
    }

}