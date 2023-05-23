/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.R
import com.patrykandpatrick.vico.compose.chart.pie.PieChart
import com.patrykandpatrick.vico.compose.chart.pie.label.inside
import com.patrykandpatrick.vico.compose.chart.pie.label.outside
import com.patrykandpatrick.vico.compose.chart.pie.slice.slice
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.LocalChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.chart.pie.label.SliceLabel
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.FloatPieEntry
import com.patrykandpatrick.vico.core.entry.pieEntryModelOf
import com.patrykandpatrick.vico.databinding.PieChart1Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem

private val model = pieEntryModelOf(
    FloatPieEntry(value = 1f, label = "One"),
    FloatPieEntry(value = 2f, label = "Two"),
    FloatPieEntry(value = 3f, label = "Three"),
    FloatPieEntry(value = 1f, label = "Four"),
)

@Composable
internal fun PieChart1(uiSystem: UISystem) {
    when (uiSystem) {
        UISystem.Compose -> ComposePieChart1()
        UISystem.Views -> ViewPieChart1()
    }
}

internal val startAngleInfiniteTransition: State<Float>
    @Composable get() = rememberInfiniteTransition(label = "startAngle infinite transition")
        .animateFloat(
            label = "startAngle animation",
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                tween(
                    durationMillis = 8_000,
                    easing = LinearEasing,
                ),
            ),
        )

@Composable
private fun ComposePieChart1() {
    val startAngle by startAngleInfiniteTransition

    val labelOutside = SliceLabel.outside(
        textComponent = textComponent(
            color = Color.Black,
            padding = dimensionsOf(horizontal = 4.dp, vertical = 2.dp),
            background = shapeComponent(
                shape = Shapes.pillShape,
                color = colorResource(id = R.color.pieChartSlice4Color),
            ),
        ),
        lineColor = Color.Black,
        angledSegmentLength = 24.dp,
    )

    val labelInside = SliceLabel.inside(
        textComponent = textComponent(
            color = Color.White,
            padding = dimensionsOf(horizontal = 4.dp, vertical = 2.dp),
            background = shapeComponent(shape = Shapes.pillShape, color = Color.Black),
            margins = dimensionsOf(4.dp),
        ),
    )

    ProvideChartStyle(
        LocalChartStyle.current.run {
            copy(
                pieChart = pieChart.copy(
                    spacing = 12.dp,
                    sliceLabel = labelInside,
                ),
            )
        },
    ) {
        PieChart(
            modifier = Modifier
                .aspectRatio(1f)
                .background(Color.White),
            model = model,
            startAngle = startAngle,
            slices = listOf(
                slice(
                    color = colorResource(id = R.color.pieChartSlice1Color),
                ),
                slice(
                    color = colorResource(id = R.color.pieChartSlice2Color),
                ),
                slice(
                    color = Color.Transparent,
                    strokeColor = colorResource(id = R.color.pieChartSlice3Color),
                    strokeWidth = 2.dp,
                ),
                slice(
                    color = colorResource(id = R.color.pieChartSlice4Color),
                    offsetFromCenter = 18.dp,
                    label = labelOutside,
                ),
            ),
        )
    }
}

@Composable
private fun ViewPieChart1() {
    val startAngle by startAngleInfiniteTransition

    AndroidViewBinding(factory = PieChart1Binding::inflate) {
        pieChartView.setModel(model)
        pieChartView.startAngle = startAngle
    }
}
