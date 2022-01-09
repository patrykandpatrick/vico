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

package pl.patrykgoworowski.vico.view.layout

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.axis.model.ChartModel
import pl.patrykgoworowski.vico.core.context.DefaultExtras
import pl.patrykgoworowski.vico.core.context.Extras
import pl.patrykgoworowski.vico.core.context.MeasureContext

internal data class MutableMeasureContext(
    override val canvasBounds: RectF,
    override var density: Float,
    override var fontScale: Float,
    override var isLtr: Boolean,
    override var isHorizontalScrollEnabled: Boolean,
    override var horizontalScroll: Float,
    override var zoom: Float,
    override val chartModel: ChartModel,
) : MeasureContext, Extras by DefaultExtras()
