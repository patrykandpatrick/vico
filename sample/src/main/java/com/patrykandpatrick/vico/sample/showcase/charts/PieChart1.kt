/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.sample.showcase.charts

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.R
import com.patrykandpatrick.vico.compose.chart.pie.PieChartHost
import com.patrykandpatrick.vico.compose.chart.pie.label.rememberInsideLabel
import com.patrykandpatrick.vico.compose.chart.pie.slice.rememberSlice
import com.patrykandpatrick.vico.compose.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.LocalChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.chart.pie.label.SliceLabel
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.model.PieChartModelProducer
import com.patrykandpatrick.vico.databinding.PieChart1Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem

private const val ANGLE_ANIM_DURATION = 6_000

@Composable
internal fun PieChart1(
    uiSystem: UISystem,
    pieEntryModelProducer: PieChartModelProducer,
) {
    val startAngle by rememberInfiniteTransition(label = "startAngle infinite transition")
        .animateFloat(
            label = "startAngle animation",
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    tween(
                        durationMillis = ANGLE_ANIM_DURATION,
                        easing = LinearEasing,
                    ),
                ),
        )

    when (uiSystem) {
        UISystem.Compose -> ComposePieChart1(startAngle, pieEntryModelProducer)
        UISystem.Views -> ViewPieChart1(startAngle, pieEntryModelProducer)
    }
}

@Composable
private fun ComposePieChart1(
    startAngle: Float,
    modelProducer: PieChartModelProducer,
) {
    val labelInside =
        SliceLabel.rememberInsideLabel(
            textComponent =
                rememberTextComponent(
                    color = Color.White,
                    padding = dimensionsOf(horizontal = 4.dp, vertical = 2.dp),
                    background = rememberShapeComponent(shape = Shapes.pillShape, color = Color.Black),
                    margins = dimensionsOf(2.dp),
                ),
        )

    ProvideChartStyle(
        LocalChartStyle.current.run {
            copy(
                pieChart =
                    pieChart.copy(
                        spacing = 4.dp,
                        sliceLabel = labelInside,
                    ),
            )
        },
    ) {
        PieChartHost(
            modelProducer = modelProducer,
            startAngle = startAngle,
            slices =
                listOf(
                    rememberSlice(
                        color = colorResource(id = R.color.pieChartSlice1Color),
                    ),
                    rememberSlice(
                        color = colorResource(id = R.color.pieChartSlice2Color),
                    ),
                    rememberSlice(
                        color = Color.Transparent,
                        strokeColor = colorResource(id = R.color.pieChartSlice3Color),
                        strokeWidth = 2.dp,
                    ),
                    rememberSlice(
                        color = colorResource(id = R.color.pieChartSlice4Color),
                    ),
                    rememberSlice(
                        color = colorResource(id = R.color.pieChartSlice5Color),
                    ),
                    rememberSlice(
                        color = colorResource(id = R.color.pieChartSlice6Color),
                    ),
                    rememberSlice(
                        color = colorResource(id = R.color.pieChartSlice7Color),
                    ),
                    rememberSlice(
                        color = colorResource(id = R.color.pieChartSlice8Color),
                    ),
                ),
        )
    }
}

@Composable
private fun ViewPieChart1(
    startAngle: Float,
    modelProducer: PieChartModelProducer,
) {
    AndroidViewBinding(factory = PieChart1Binding::inflate) {
        pieChartView.modelProducer = modelProducer
        pieChartView.startAngle = startAngle
    }
}
