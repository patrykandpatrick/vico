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

package com.patrykandpatrick.vico.core.entry.diff

/**
 * An extension of [DiffProcessor] that provides additional methods for use in difference animations for Cartesian
 * charts.
 */
public interface CartesianChartDiffProcessor<Entry> : DiffProcessor<Entry> {

    /**
     * Creates an intermediate y-value range for use in difference animations.
     * @param progress the balance between the initial and target range. A value of `Of` yields the initial range, and
     * a value of `1f` yields the target range.
     */
    public fun yRangeProgressDiff(progress: Float): ClosedFloatingPointRange<Float>

    /**
     * Creates an intermediate stacked y-value range for use in difference animations.
     * @param progress the balance between the initial and target range. A value of `Of` yields the initial range, and
     * a value of `1f` yields the target range.
     */
    public fun stackedYRangeProgressDiff(progress: Float): ClosedFloatingPointRange<Float>
}
