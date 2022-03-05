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

package pl.patrykgoworowski.vico.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.R
import pl.patrykgoworowski.vico.app.ChartStyleOverrideManager
import pl.patrykgoworowski.vico.compose.style.ChartStyle
import pl.patrykgoworowski.vico.core.extension.piRad

private const val PI_RAD_BY_THIRTY_DEGREES = 6f
private val thirtyDegrees = (1f / PI_RAD_BY_THIRTY_DEGREES).piRad
private val rotationSliderSteps = (2f.piRad / thirtyDegrees).toInt() - 1

@Composable
internal fun ConfigurationSheet(
    chartStyle: ChartStyle,
    chartStyleOverrideManager: ChartStyleOverrideManager,
    setFirstSheetItemHeight: (Int) -> Unit,
) {
    Column(modifier = Modifier.navigationBarsPadding()) {
        Column(
            modifier = Modifier.onGloballyPositioned {
                setFirstSheetItemHeight(it.size.height)
            },
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)),
            )
            SliderPreference(
                value = chartStyleOverrideManager.chartStyleOverrides.axis.axisLabelRotationDegrees,
                default = chartStyle.axis.axisLabelRotationDegrees,
                valueRange = (-1f).piRad..1f.piRad,
                steps = rotationSliderSteps,
                label = stringResource(id = R.string.axis_label_rotation_title),
                onValueChange = {
                    chartStyleOverrideManager.updateChartStyle(
                        axisLabelRotationDegrees = it,
                    )
                },
            )
        }
    }
}
