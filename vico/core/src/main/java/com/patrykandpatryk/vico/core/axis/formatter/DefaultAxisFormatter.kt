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

package com.patrykandpatryk.vico.core.axis.formatter

import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.model.ChartModel

/**
 * The default implementation of [AxisValueFormatter]. This converts [Float]s to [String]s.
 */
public class DefaultAxisFormatter<Position : AxisPosition> : AxisValueFormatter<Position> {
    override fun formatValue(
        value: Float,
        chartModel: ChartModel,
    ): String = value.toString()
}
