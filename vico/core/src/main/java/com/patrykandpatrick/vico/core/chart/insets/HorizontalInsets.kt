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

package com.patrykandpatrick.vico.core.chart.insets

import com.patrykandpatrick.vico.core.chart.Chart

/**
 * Used to apply horizontal insets to [Chart]s.
 *
 * @see ChartInsetter
 * @see Insets
 */
public interface HorizontalInsets {

    /**
     * Sets the start and end insets.
     */
    public fun set(start: Float, end: Float)

    /**
     * For the start and end insets, updates the value of the inset to the corresponding
     * provided value if the provided value is greater than the current value.
     */
    public fun setValuesIfGreater(start: Float, end: Float)
}
