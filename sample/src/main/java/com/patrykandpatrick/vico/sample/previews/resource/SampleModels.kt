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

package com.patrykandpatrick.vico.sample.previews.resource

import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel

public val shortColumnModel =
  CartesianChartModel(ColumnCartesianLayerModel.build { series(1, 2, 4, 8, 3) })

public val mediumColumnModel =
  CartesianChartModel(
    ColumnCartesianLayerModel.build { series(1, 2, 4, 8, 3, 10, 4, 7, 2, 6, 4, 8) }
  )

public val shortLineModel =
  CartesianChartModel(LineCartesianLayerModel.build { series(1, 2, 4, 8, 3) })

public val mediumLineModel =
  CartesianChartModel(LineCartesianLayerModel.build { series(1, 2, 4, 8, 3, 10, 4, 7, 2, 6, 4, 8) })
