/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.cartesian.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerController

/** Creates and remembers a [CartesianMarkerController] that shows the marker on press. */
@Composable
public fun CartesianMarkerController.Companion.rememberShowOnPress(): CartesianMarkerController =
  remember { showOnPress() }

/** Creates and remembers a [CartesianMarkerController] that shows the marker on hover. */
@Composable
public fun CartesianMarkerController.Companion.rememberShowOnHover(): CartesianMarkerController =
  remember { showOnHover() }

/** Creates and remembers a [CartesianMarkerController] that toggles the marker visibility on tap. */
@Composable
public fun CartesianMarkerController.Companion.rememberToggleOnTap(): CartesianMarkerController =
  remember { toggleOnTap() }
