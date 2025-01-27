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

package com.patrykandpatrick.vico.multiplatform.common

import androidx.compose.ui.geometry.Rect

/** Defines an abstract component that has some physical bounds. */
public interface Bounded {
  /** The bounds of the abstract component. */
  public var bounds: Rect

  /** Sets the coordinates of the bounds to the provided values. */
  public fun setBounds(left: Number, top: Number, right: Number, bottom: Number) {
    bounds = Rect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
  }
}
