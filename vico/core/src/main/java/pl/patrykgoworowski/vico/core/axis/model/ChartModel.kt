/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.axis.model

import pl.patrykgoworowski.vico.core.chart.entry.collection.EntryModel

public interface ChartModel {

    /**
     * The minimum value on the x-axis. It is equal to [EntryModel.minX] from [entryModel] by default
     * but may be overridden.
     */
    public val minX: Float

    /**
    * The maximum value on the x-axis. It is equal to [EntryModel.maxX] from [entryModel] by default
    * but may be overridden.
    */
    public val maxX: Float

    /**
    * The minimum value on the y-axis. It is equal to [EntryModel.minY] from [entryModel] by default
    * but may be overridden.
    */
    public val minY: Float

    /**
    * The maximum value on the y-axis. It is equal to [EntryModel.maxY] from [entryModel] by default
    * but may be overridden.
    */
    public val maxY: Float

    /**
     * The source of the entries drawn on the chart. It holds default values for [minX], [maxX], [minY], and [maxY].
     */
    public val entryModel: EntryModel
}
