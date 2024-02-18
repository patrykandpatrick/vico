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

package com.patrykandpatrick.vico.core.pie

import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.cartesian.model.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.model.CartesianLayerModel
import com.patrykandpatrick.vico.core.common.ExtraStore
import com.patrykandpatrick.vico.core.common.extension.random

/**
 * Generates randomized [CartesianLayerModel.Partial]s and [CartesianChartModel]s.
 */
public object RandomPieModelGenerator {
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public val valueRange: IntRange = 1..8

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public val sliceRange: IntRange = 2..8

    /**
     * Generates a randomized [PieModel.Partial] with the specified number of slices and value ranges.
     */
    public fun getRandomPartial(
        sliceRange: IntRange = RandomPieModelGenerator.sliceRange,
        valueRange: IntRange = RandomPieModelGenerator.valueRange,
    ): PieModel.Partial =
        PieModel.partial(
            buildList {
                repeat(sliceRange.random()) {
                    add(
                        PieModel.Entry(
                            value = valueRange.random().toFloat(),
                        ),
                    )
                }
            },
        )

    /**
     * Generates a randomized [PieModel] with the specified numbers of slices and value ranges.
     */
    public fun getRandomModel(
        sliceRange: IntRange = RandomPieModelGenerator.sliceRange,
        valueRange: IntRange = RandomPieModelGenerator.valueRange,
    ): PieModel = getRandomPartial(sliceRange, valueRange).complete(ExtraStore.empty)
}
