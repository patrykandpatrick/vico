/*
 * Copyright (c) 2022. Patryk Goworowski
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

package pl.patrykgoworowski.vico.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import pl.patrykgoworowski.vico.compose.style.ChartStyle

internal class ChartStyleOverrideManager {

    internal var chartStyleOverrides: ChartStyleOverrides by mutableStateOf(value = ChartStyleOverrides())

    internal fun updateChartStyle(
        axisLabelRotationDegrees: Float? = chartStyleOverrides.axis.axisLabelRotationDegrees,
    ) {
        chartStyleOverrides = chartStyleOverrides.copy(
            axis = chartStyleOverrides.axis.copy(
                axisLabelRotationDegrees = axisLabelRotationDegrees,
            )
        )
    }

    internal companion object {

        internal fun overrideChartStyle(
            chartStyle: ChartStyle,
            chartStyleOverrides: ChartStyleOverrides,
        ): ChartStyle =
            chartStyle.copy(
                axis = chartStyle.axis.copy(
                    axisLabelRotationDegrees = chartStyleOverrides.axis.axisLabelRotationDegrees
                        ?: chartStyle.axis.axisLabelRotationDegrees,
                ),
            )
    }
}
