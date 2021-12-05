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

import pl.patrykgoworowski.vico.core.entry.ChartEntryModelProducer
import pl.patrykgoworowski.vico.core.entry.ChartModelProducer

/**
 * The utility class used [ChartModelProducer] subclasses such as [ChartEntryModelProducer].
 *
 * It is used to animate y-axis values after data update.
 *
 * @see [ChartEntryModelProducer]
 */
public interface DiffAnimator {

    /**
     * Current progress of animation.
     * Ranges between 0f and 1f.
     */
    public val currentProgress: Float

    /**
     * Starts an animation.
     * @param [onProgress] The callback yielding the current animation progress on each animation progress update.
     */
    public fun start(onProgress: (progress: Float) -> Unit)

    /**
     * Cancels an animation.
     */
    public fun cancel()
}
