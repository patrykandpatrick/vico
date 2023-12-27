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

package com.patrykandpatrick.vico.compose.chart.pie

import android.graphics.RectF
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.chart.ChartBox
import com.patrykandpatrick.vico.compose.model.collectAsState
import com.patrykandpatrick.vico.compose.model.defaultDiffAnimationSpec
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.chart.pie.PieChart
import com.patrykandpatrick.vico.core.chart.pie.Size
import com.patrykandpatrick.vico.core.chart.pie.slice.Slice
import com.patrykandpatrick.vico.core.draw.drawContext
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.core.extension.spToPx
import com.patrykandpatrick.vico.core.model.PieChartModelProducer
import com.patrykandpatrick.vico.core.model.PieModel

/**
 * Draws a pie chart.
 *
 * @param modifier the modifier to be applied to the chart.
 * @param slices the [List] of [Slice]s which define the appearance of each slice of the pie chart.
 * @param pieChartModelProducer creates and updates the [PieChartModelProducer] for the chart.
 * @param spacing defines spacing between [slices].
 * @param outerSize defines the size of the chart.
 * @param innerSize defines the size of the hole in the middle of the chart.
 * @param startAngle defines the angle at which the first slice starts.
 */
@Composable
public fun PieChartHost(
    pieChartModelProducer: PieChartModelProducer,
    modifier: Modifier = Modifier,
    slices: List<Slice> = currentChartStyle.pieChart.slices,
    spacing: Dp = currentChartStyle.pieChart.spacing,
    outerSize: Size.OuterSize = currentChartStyle.pieChart.outerSize,
    innerSize: Size.InnerSize = currentChartStyle.pieChart.innerSize,
    startAngle: Float = currentChartStyle.pieChart.startAngle,
    diffAnimationSpec: AnimationSpec<Float> = defaultDiffAnimationSpec,
    elevationOverlayColor: Color = currentChartStyle.elevationOverlayColor,
    runInitialAnimation: Boolean = true,
    onEmptyState: (@Composable () -> Unit)? = null,
) {
    val pieChart =
        remember {
            PieChart(
                slices = slices,
                spacingDp = spacing.value,
                innerSize = innerSize,
                outerSize = outerSize,
                startAngle = startAngle,
            )
        }.apply {
            this.slices = slices
            this.spacingDp = spacing.value
            this.innerSize = innerSize
            this.outerSize = outerSize
            this.startAngle = startAngle
        }

    val model =
        pieChartModelProducer.collectAsState(
            chart = pieChart,
            producerKey = pieChartModelProducer,
            animationSpec = diffAnimationSpec,
            runInitialAnimation = runInitialAnimation,
        )

    ChartBox(modifier = modifier) {
        model.value?.also { model ->
            PieChartHost(
                model = model,
                pieChart = pieChart,
                modifier = modifier,
                elevationOverlayColor = elevationOverlayColor,
            )
        } ?: onEmptyState?.invoke()
    }
}

/**
 * Draws a pie chart.
 *
 * @param model the [PieEntryModel] containing data to render the pie chart.
 * @param pieChart TODO,
 * @param modifier the modifier to be applied to the chart.
 * @param elevationOverlayColor the color to use for the elevation overlay.
 */
@Composable
// TODO Legend
public fun PieChartHost(
    model: PieModel,
    pieChart: PieChart,
    modifier: Modifier = Modifier,
    elevationOverlayColor: Color = currentChartStyle.elevationOverlayColor,
) {
    val bounds = remember { RectF() }
    val density = LocalDensity.current.density
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val context = LocalContext.current

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(DefaultDimens.CHART_HEIGHT.dp),
    ) {
        bounds.set(left = 0, top = 0, right = size.width, bottom = size.height)

        pieChart.setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom)

        val drawContext =
            drawContext(
                canvas = drawContext.canvas.nativeCanvas,
                density = density,
                isLtr = isLtr,
                elevationOverlayColor = elevationOverlayColor.toArgb().toLong(),
                spToPx = context::spToPx,
            )

        pieChart.draw(
            context = drawContext,
            model = model,
        )
    }
}
