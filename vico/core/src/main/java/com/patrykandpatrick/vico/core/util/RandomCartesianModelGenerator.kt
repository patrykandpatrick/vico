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

package com.patrykandpatrick.vico.core.util

import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.extension.random
import com.patrykandpatrick.vico.core.model.CartesianChartModel
import com.patrykandpatrick.vico.core.model.CartesianLayerModel
import com.patrykandpatrick.vico.core.model.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.model.LineCartesianLayerModel

/**
 * Generates randomized [CartesianLayerModel.Partial]s and [CartesianChartModel]s.
 */
public object RandomCartesianModelGenerator {
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public val defaultX: IntProgression = 0..96

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public val defaultY: ClosedFloatingPointRange<Float> = 2f..20f

    /**
     * Generates a randomized [ColumnCartesianLayerModel.Partial] with the specified number of series and value ranges.
     */
    public fun getRandomColumnLayerModelPartial(
        seriesCount: Int = 1,
        x: IntProgression = defaultX,
        y: ClosedFloatingPointRange<Float> = defaultY,
    ): ColumnCartesianLayerModel.Partial =
        ColumnCartesianLayerModel.partial { repeat(seriesCount) { series(x.toList(), x.map { y.random() }) } }

    /**
     * Generates a randomized [LineCartesianLayerModel.Partial] with the specified number of series and value ranges.
     */
    public fun getRandomLineLayerModelPartial(
        seriesCount: Int = 1,
        x: IntProgression = defaultX,
        y: ClosedFloatingPointRange<Float> = defaultY,
    ): LineCartesianLayerModel.Partial =
        LineCartesianLayerModel.partial { repeat(seriesCount) { series(x.toList(), x.map { y.random() }) } }

    /**
     * Generates a randomized [CartesianChartModel] with the specified numbers of series and value ranges.
     */
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
