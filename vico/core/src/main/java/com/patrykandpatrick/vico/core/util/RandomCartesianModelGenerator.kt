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

package com.patrykandpatrick.vico.core.util

import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.extension.random
import com.patrykandpatrick.vico.core.model.CartesianChartModel
import com.patrykandpatrick.vico.core.model.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.model.LineCartesianLayerModel

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public object RandomCartesianModelGenerator {
    public val defaultX: IntProgression = 0..96
    public val defaultY: ClosedFloatingPointRange<Float> = 2f..20f

    private fun getRandomColumnLayerModelPartial(
        seriesCount: Int = 1,
        x: IntProgression = defaultX,
        y: ClosedFloatingPointRange<Float> = defaultY,
    ): ColumnCartesianLayerModel.Partial =
        ColumnCartesianLayerModel.partial { repeat(seriesCount) { series(x.toList(), x.map { y.random() }) } }

    private fun getRandomLineLayerModelPartial(
        seriesCount: Int = 1,
        x: IntProgression = defaultX,
        y: ClosedFloatingPointRange<Float> = defaultY,
    ): LineCartesianLayerModel.Partial =
        LineCartesianLayerModel.partial { repeat(seriesCount) { series(x.toList(), x.map { y.random() }) } }

    public fun getRandomModel(
        columnSeriesCount: Int = 1,
        lineSeriesCount: Int = 1,
        x: IntProgression = defaultX,
        y: ClosedFloatingPointRange<Float> = defaultY,
    ): CartesianChartModel =
        CartesianChartModel(
            buildList {
                add(getRandomColumnLayerModelPartial(columnSeriesCount, x, y).complete())
                add(getRandomLineLayerModelPartial(lineSeriesCount, x, y).complete())
            },
        )
}
