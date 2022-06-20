/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.core.marker

/**
 * Allows for listening to [Marker] visibility changes.
 */
public fun interface MarkerVisibilityChangeListener {

    /**
     * Called when the linked [Marker]’s visibility changes.
     *
     * @param isVisible whether the linked [Marker] is visible.
     * @param marker the linked [Marker], whose visibility has changed.
     */
    public fun onMarkerVisibilityChanged(
        isVisible: Boolean,
        marker: Marker,
    )
}
