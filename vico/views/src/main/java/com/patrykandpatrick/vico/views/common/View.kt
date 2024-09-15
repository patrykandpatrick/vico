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

package com.patrykandpatrick.vico.views.common

import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.core.view.updatePadding
import com.patrykandpatrick.vico.core.common.Point

internal val Int.specSize: Int
  get() = View.MeasureSpec.getSize(this)

internal val Int.specMode: Int
  get() = View.MeasureSpec.getMode(this)

internal var View.verticalPadding: Int
  get() = paddingTop + paddingBottom
  set(value) {
    updatePadding(top = value / 2, bottom = value / 2)
  }

internal var View.horizontalPadding: Int
  get() = paddingLeft + paddingRight
  set(value) {
    updatePadding(left = value / 2, right = value / 2)
  }

internal fun OverScroller.fling(
  startX: Int = 0,
  startY: Int = 0,
  velocityX: Int = 0,
  velocityY: Int = 0,
  minScrollX: Int = 0,
  maxScrollX: Int = Int.MAX_VALUE,
) {
  fling(startX, startY, velocityX, velocityY, minScrollX, maxScrollX, Int.MIN_VALUE, Int.MAX_VALUE)
}

internal val MotionEvent.point: Point
  get() = Point(x, y)
