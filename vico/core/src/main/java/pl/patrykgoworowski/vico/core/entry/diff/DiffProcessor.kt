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

package pl.patrykgoworowski.vico.core.entry.diff

import pl.patrykgoworowski.vico.core.entry.ChartEntry

/**
 * Processes the difference between two collections of [ChartEntry] instances and generates intermediate collections
 * for use in difference animations.
 */
public interface DiffProcessor<Entry : ChartEntry> {

    /**
     * Sets the initial and target collections of [ChartEntry] instances.
     * @param old the initial collection.
     * @param new the target collection.
     */
    public fun setEntries(old: List<List<Entry>>, new: List<List<Entry>>)

    /**
     * Reuses the current target collection of [ChartEntry] instances as the initial collection and sets a new target
     * collection.
     * @param new the target collection.
     */
    public fun setEntries(new: List<List<Entry>>)

    /**
     * Creates an intermediate collection of [ChartEntry] instances for use in difference animations.
     * @param progress the balance between the initial and target collections. A value of `0f` yields the initial
     * collection, and a value of `1f` yields the target collection.
     */
    public fun progressDiff(progress: Float): List<List<Entry>>

    public fun yRangeProgressDiff(progress: Float): ClosedFloatingPointRange<Float>

    public fun stackedYRangeProgressDiff(progress: Float): ClosedFloatingPointRange<Float>
}
