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

package com.patrykandpatrick.vico.core.marker

/**
 * Allows for listening to [Marker] visibility changes.
 */
public interface MarkerVisibilityChangeListener {

    /**
     * Called when the linked [Marker] is shown.
     *
     * @param marker the linked [Marker], which has been shown.
     * @param markerEntryModels a list of [Marker.EntryModel]s, which contain information about the marked chart
     * entries.
     */
    public fun onMarkerShown(
        marker: Marker,
        markerEntryModels: List<Marker.EntryModel>,
    ) {
        @Suppress("DEPRECATION")
        onMarkerVisibilityChanged(isVisible = true, marker = marker)
    }

    /**
     * Called when the linked [Marker] moves (that is, when there’s a change in which chart entries it highlights).
     *
     * @param marker the linked [Marker], which moved.
     * @param markerEntryModels a list of [Marker.EntryModel]s, which contain information about the marked chart
     * entries.
     */
    public fun onMarkerMoved(
        marker: Marker,
        markerEntryModels: List<Marker.EntryModel>,
    ): Unit = Unit

    /**
     * Called when the linked [Marker] is hidden.
     *
     * @param marker the linked [Marker], which has been hidden.
     */
    public fun onMarkerHidden(marker: Marker) {
        @Suppress("DEPRECATION")
        onMarkerVisibilityChanged(isVisible = false, marker = marker)
    }

    /**
     * Called when the linked [Marker]ʼs visibility changes.
     *
     * @param isVisible whether the linked [Marker] is visible.
     * @param marker the linked [Marker], whose visibility has changed.
     */
    @Deprecated("Use `onMarkerShown` and `onMarkerHidden` instead.")
    public fun onMarkerVisibilityChanged(
        isVisible: Boolean,
        marker: Marker,
    ): Unit = Unit
}
