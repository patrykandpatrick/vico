/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.chart.scale

/**
 * Defines whether the content of a scrollable chart should be scaled up when the entry count and intrinsic segment
 * width are such that, at a scale factor of 1, an empty space would be visible near the end edge of the chart.
 */
public enum class AutoScaleUp {

    /**
     * Scales up the chart to prevent any empty space from being visible.
     */
    Full,

    /**
     * Leaves the chart’s scale unaffected. Empty space may be visible.
     */
    None,
}
