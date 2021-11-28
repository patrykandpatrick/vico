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
     * Minimum value on X axis. By default it equals to [EntryModel.minX] in [entryModel],
     * but may be overridden.
     */
    public val minX: Float

    /**
     * Maximum value on X axis. By default it equals to [EntryModel.maxX] in [entryModel],
     * but may be overridden.
     */
    public val maxX: Float

    /**
     * Minimum value on Y axis. By default it equals to [EntryModel.minY] in [entryModel],
     * but may be overridden.
     */
    public val minY: Float

    /**
     * Maximum value on Y axis. By default it equals to [EntryModel.maxY] in [entryModel],
     * but may be overridden.
     */
    public val maxY: Float

    /**
     * Source of entries drawn on chart. It holds default values for [minX], [maxX], [minY], [maxY].
     */
    public val entryModel: EntryModel
}
