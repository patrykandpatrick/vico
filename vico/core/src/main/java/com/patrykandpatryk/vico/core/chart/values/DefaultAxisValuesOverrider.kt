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

package com.patrykandpatryk.vico.core.chart.values

import com.patrykandpatryk.vico.core.entry.ChartEntryModel

/**
 * An [AxisValuesOverrider] which inherits all of its values from [ChartEntryModel].
 *
 * @see AxisValuesOverrider
 */
public open class DefaultAxisValuesOverrider : AxisValuesOverrider<ChartEntryModel> {

    override fun getMinX(model: ChartEntryModel): Float = model.minX

    override fun getMaxX(model: ChartEntryModel): Float = model.maxX

    override fun getMinY(model: ChartEntryModel): Float = model.minY

    override fun getMaxY(model: ChartEntryModel): Float = model.maxY
}
