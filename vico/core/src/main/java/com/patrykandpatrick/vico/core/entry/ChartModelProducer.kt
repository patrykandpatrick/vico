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

package com.patrykandpatrick.vico.core.entry

import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.diff.MutableExtraStore

/**
 * Generates [ChartEntryModel]s and handles difference animations.
 *
 * @see ChartEntryModelProducer
 * @see ComposedChartEntryModelProducer
 */
public interface ChartModelProducer<Model : ChartEntryModel> {

    /**
     * Returns the [ChartEntryModel] or, if no [ChartEntryModel] is available, `null`.
     */
    public fun getModel(): Model?

    /**
     * Returns the [ChartEntryModel] or, if no [ChartEntryModel] is available, throws an exception.
     */
    public fun requireModel(): Model = getModel()!!

    /**
     * Creates an intermediate [ChartEntryModel] for difference animations. [fraction] is the balance between the
     * initial and target [ChartEntryModel]s.
     */
    @Deprecated(
        "Use the function passed to the `startAnimation` lambda of `registerForUpdates`. In custom " +
            "`ChartModelProducer` implementations, this deprecated function can do nothing.",
    )
    public suspend fun transformModel(key: Any, fraction: Float)

    /**
     * Registers an update listener associated with a [key]. [cancelAnimation] and [startAnimation] are
     * called after a data update is requested, with [cancelAnimation] being called before the update starts
     * being processed, and [startAnimation] being called once the update has been processed. [updateChartValues]
     * updates the chart’s [ChartValues] and returns its [ChartValuesProvider]. [onModelCreated] is called when a new
     * [Model] has been generated.
     */
    public fun registerForUpdates(
        key: Any,
        cancelAnimation: () -> Unit,
        startAnimation: (transformModel: suspend (chartKey: Any, fraction: Float) -> Unit) -> Unit,
        getOldModel: () -> Model?,
        modelTransformerProvider: Chart.ModelTransformerProvider?,
        extraStore: MutableExtraStore,
        updateChartValues: (Model?) -> ChartValuesProvider,
        onModelCreated: (Model?, ChartValuesProvider) -> Unit,
    ) {
        @Suppress("DEPRECATION")
        registerForUpdates(
            key,
            cancelAnimation,
            startAnimation,
            getOldModel,
            modelTransformerProvider,
            extraStore,
            updateChartValues,
        ) { model ->
            onModelCreated(model, updateChartValues(model))
        }
    }

    /**
     * Registers an update listener associated with a [key]. [cancelAnimation] and [startAnimation] are called after a
     * data update is requested, with [cancelAnimation] being called before the update starts being processed (at which
     * point [transformModel] should stop being used), and [startAnimation] being called once the update has been
     * processed (at which point it’s safe to use [transformModel]). [updateChartValues] updates the chart’s
     * [ChartValues] and returns its [ChartValuesProvider]. [onModelCreated] is called when a new [Model] has been
     * generated.
     */
    @Deprecated(
        "Use the overload in which `onModelCreated` has two parameters. In custom `ChartModelProducer` " +
            "implementations, the aforementioned overload should be overridden, and this one can do nothing.",
    )
    public fun registerForUpdates(
        key: Any,
        cancelAnimation: () -> Unit,
        startAnimation: (transformModel: suspend (chartKey: Any, fraction: Float) -> Unit) -> Unit,
        getOldModel: () -> Model?,
        modelTransformerProvider: Chart.ModelTransformerProvider?,
        extraStore: MutableExtraStore,
        updateChartValues: (Model?) -> ChartValuesProvider,
        onModelCreated: (Model?) -> Unit,
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
