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

package com.patrykandpatrick.vico.compose.chart.pie

import androidx.compose.ui.unit.Dp
import com.patrykandpatrick.vico.core.pie.Size.InnerSize
import com.patrykandpatrick.vico.core.pie.Size.OuterSize

/**
 * Creates a fixed [OuterSize], with the size specified by the [maxDiameter] (in dp unit).
 * If the available space is smaller than [maxDiameter], all available space is used.
 */
public fun OuterSize.Companion.fixed(maxDiameter: Dp): OuterSize = fixed(maxDiameter.value)

/**
 * Creates a fixed [InnerSize], with the size specified by the [maxDiameter] (in dp unit).
 * If the available space is smaller than [maxDiameter], all available space is used.
 */
public fun InnerSize.Companion.fixed(maxDiameter: Dp): InnerSize = fixed(maxDiameter.value)
