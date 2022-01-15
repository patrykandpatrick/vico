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

package pl.patrykgoworowski.vico.compose.axis.vertical

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import pl.patrykgoworowski.vico.compose.axis.axisGuidelineComponent
import pl.patrykgoworowski.vico.compose.axis.axisLabelComponent
import pl.patrykgoworowski.vico.compose.axis.axisLineComponent
import pl.patrykgoworowski.vico.compose.axis.axisTickComponent
import pl.patrykgoworowski.vico.compose.style.currentChartStyle
import pl.patrykgoworowski.vico.core.axis.Axis
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis
import pl.patrykgoworowski.vico.core.axis.vertical.createVerticalAxis
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.text.TextComponent

@Composable
public fun startAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
): VerticalAxis<AxisPosition.Vertical.Start> = createVerticalAxis {
    this.label = label
    this.axis = axis
    this.tick = tick
    this.guideline = guideline
    this.valueFormatter = valueFormatter
    tickLengthDp = tickLength.value
    this.sizeConstraint = sizeConstraint
}

@Composable
public fun endAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
): VerticalAxis<AxisPosition.Vertical.End> = createVerticalAxis {
    this.label = label
    this.axis = axis
    this.tick = tick
    this.guideline = guideline
    this.valueFormatter = valueFormatter
    this.tickLengthDp = tickLength.value
    this.sizeConstraint = sizeConstraint
}
