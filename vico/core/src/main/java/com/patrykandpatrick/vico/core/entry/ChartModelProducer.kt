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

package com.patrykandpatrick.vico.core.entry

import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.entry.diff.MutableDrawingModelStore

/**
 * A [Model] producer that can deliver generated [Model]s asynchronously. It supports difference animations.
 *
 * @see ChartEntryModel
 */
public interface ChartModelProducer<Model : ChartEntryModel> {

    /**
     * Returns the [ChartEntryModel] for this [ChartModelProducer] synchronously.
     */
    public fun getModel(): Model

    /**
     * Calculates an intermediate list of entries for difference animations for the associated [key], where [progress]
     * is the balance between the previous and current lists of entries.
     */
    public suspend fun progressModel(key: Any, progress: Float)

    /**
     * Registers an update listener associated with a [key]. [cancelAnimation] and [startAnimation] are
     * called after a data update is requested, with [cancelAnimation] being called before the update starts
     * being processed (at which point [progressModel] should stop being used), and [startAnimation] being
     * called once the update has been processed (at which point it’s safe to use [progressModel]). [updateChartValues]
     * updates the chart’s [ChartValues] and returns its [ChartValuesManager]. [onModelCreated] is called when a new
     * [Model] has been generated.
     */
    public fun registerForUpdates(
        key: Any,
        cancelAnimation: () -> Unit,
        startAnimation: (progressModel: suspend (chartKey: Any, progress: Float) -> Unit) -> Unit,
        getOldModel: () -> Model?,
        modelTransformerProvider: Chart.ModelTransformerProvider?,
        drawingModelStore: MutableDrawingModelStore,
        updateChartValues: (Model) -> ChartValuesManager,
        onModelCreated: (Model) -> Unit,
    )

    /**
     * Checks if an update listener with the given [key] is registered.
     */
    public fun isRegistered(key: Any): Boolean

    /**
     * Unregisters the update listener associated with the given [key].
     */
    public fun unregisterFromUpdates(key: Any)
}
