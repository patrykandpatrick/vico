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

package pl.patrykgoworowski.vico.compose.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import pl.patrykgoworowski.vico.core.axis.model.ChartModel
import pl.patrykgoworowski.vico.core.context.DefaultExtras
import pl.patrykgoworowski.vico.core.context.Extras
import pl.patrykgoworowski.vico.core.context.MeasureContext

@Composable
public fun getMeasureContext(
    isHorizontalScrollEnabled: Boolean,
    zoom: Float,
    chartModel: ChartModel,
    dataModel: Any
): MeasureContext {
    val rememberedDataModel = remember { mutableStateOf(dataModel) }
    val context = remember {
        object : MeasureContext, Extras by DefaultExtras() {
            override var chartModel: ChartModel = chartModel
            override var density: Float = 0f
            override var fontScale: Float = 0f
            override var isLtr: Boolean = true
            override var isHorizontalScrollEnabled: Boolean = isHorizontalScrollEnabled
            override var zoom: Float = zoom
        }
    }
    if (rememberedDataModel.value != dataModel) {
        context.clearExtras()
        rememberedDataModel.value = dataModel
    }
    context.density = LocalDensity.current.density
    context.fontScale = LocalDensity.current.fontScale * LocalDensity.current.density
    context.isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    context.isHorizontalScrollEnabled = isHorizontalScrollEnabled
    context.zoom = zoom
    return context
}
