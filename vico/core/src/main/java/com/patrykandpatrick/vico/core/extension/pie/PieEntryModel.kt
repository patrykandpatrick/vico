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

package com.patrykandpatrick.vico.core.entry.pie

import com.patrykandpatrick.vico.core.chart.pie.PieChart

/**
 * Contains the data for a [PieChart]. Pre-calculates values needed for rendering of the [PieChart].
 *
 * It’s recommended to delegate the creation of [PieEntryModel] to [PieEntryModelProducer].
 *
 * @see [PieEntryModelProducer]
 */
public interface PieEntryModel {

    /**
     * The pie chart entries ([PieEntry] instances).
     */
    public val entries: List<PieEntry>

    /**
     * The sum of all values of the [entries].
     */
    public val sumOfValues: Float
}
