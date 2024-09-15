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

package com.patrykandpatrick.vico.core.common.shape

import android.graphics.Path
import com.patrykandpatrick.vico.core.common.MeasuringContext

/** Defines a shape. */
public fun interface Shape {
  /**
   * Adds an outline of the [Shape] to [path]. [left], [top], [right], and [bottom] define the
   * outline bounds.
   */
  public fun outline(
    context: MeasuringContext,
    path: Path,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  )

  public companion object {
    /** A rectangle with sharp corners. */
    public val Rectangle: Shape = Shape { _, path, left, top, right, bottom ->
      path.moveTo(left, top)
      path.lineTo(right, top)
      path.lineTo(right, bottom)
      path.lineTo(left, bottom)
      path.close()
    }
  }
}
