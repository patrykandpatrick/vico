/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.views.common.gesture

import android.view.GestureDetector
import android.view.MotionEvent

internal class ChartSimpleOnGestureListener(
  private val onTap: (x: Float, y: Float) -> Unit,
  private val onLongPress: (x: Float, y: Float) -> Unit,
) : GestureDetector.SimpleOnGestureListener() {
  override fun onSingleTapUp(e: MotionEvent): Boolean {
    onTap(e.x, e.y)
    return true
  }

  override fun onLongPress(e: MotionEvent) {
    onLongPress(e.x, e.y)
  }
}
