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

package com.patrykandpatrick.vico.sample.previews

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEndAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.style.LocalChartStyle
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.model.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.model.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.common.shape.Corner
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.CutCornerTreatment
import com.patrykandpatrick.vico.core.common.shape.RoundedCornerTreatment
import com.patrykandpatrick.vico.core.common.shape.Shapes

private val model = CartesianChartModel(ColumnCartesianLayerModel.build { series(1, 2, 3, 4) })

@Composable
private fun ProvidePreviewChartStyle(content: @Composable () -> Unit) {
    val chartStyle =
        LocalChartStyle.current.copy(
            axis =
                LocalChartStyle.current.axis.copy(
                    axisLabelColor = Color.Black,
                    axisLineColor = Color.Black.copy(alpha = 0.5f),
                    axisGuidelineColor = Color.Black.copy(alpha = 0.2f),
                ),
            columnLayer =
                LocalChartStyle.current.columnLayer.copy(
                    columns =
                        LocalChartStyle.current.columnLayer.columns.map {
                            rememberLineComponent(
                                color = Color.Gray,
                                thickness = it.thicknessDp.dp,
                                shape = it.shape,
                                dynamicShader = it.dynamicShader,
                                margins = it.margins,
                            )
                        },
                ),
        )
    CompositionLocalProvider(LocalChartStyle provides chartStyle, content = content)
}

@Composable
@Preview(showBackground = true, widthDp = 250)
public fun HorizontalAxisTextInside() {
    ProvidePreviewChartStyle {
        val label =
            axisLabelComponent(
                background =
                    rememberShapeComponent(
                        shape =
                            CorneredShape(
                                topLeft =
                                    Corner.Relative(
                                        percentage = 50,
                                        cornerTreatment = CutCornerTreatment,
                                    ),
                                bottomRight =
                                    Corner.Relative(
                                        percentage = 50,
                                        cornerTreatment = RoundedCornerTreatment,
                                    ),
                            ),
                        color = Color.LightGray,
                        strokeColor = Color.Gray,
                        strokeWidth = 1.dp,
                    ),
                verticalPadding = 2.dp,
                horizontalPadding = 8.dp,
                verticalMargin = 4.dp,
                horizontalMargin = 4.dp,
            )
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis =
                        rememberStartAxis(
                            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                            label = label,
                        ),
                    endAxis =
                        rememberEndAxis(
                            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                            guideline = null,
                            label = label,
                        ),
                ),
            model = model,
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 250)
public fun HorizontalAxisTextInsideAndBottomAxis() {
    ProvidePreviewChartStyle {
        val label =
            axisLabelComponent(
                background =
                    rememberShapeComponent(
                        shape = Shapes.pillShape,
                        color = Color.LightGray,
                    ),
                verticalPadding = 2.dp,
                horizontalPadding = 8.dp,
                verticalMargin = 4.dp,
                horizontalMargin = 4.dp,
            )
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis =
                        rememberStartAxis(
                            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                            label = label,
                        ),
                    endAxis =
                        rememberEndAxis(
                            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                            guideline = null,
                            label = label,
                        ),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 250)
public fun HorizontalAxisTextOutside() {
    ProvidePreviewChartStyle {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis =
                        rememberStartAxis(
                            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside,
                        ),
                    endAxis =
                        rememberEndAxis(
                            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside,
                            guideline = null,
                        ),
                ),
            model = model,
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 250)
public fun HorizontalAxisGuidelineDoesNotOverlayBottomAxisLine() {
    ProvidePreviewChartStyle {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis =
                        rememberStartAxis(horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
        )
    }
}
