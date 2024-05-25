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

package com.patrykandpatrick.vico.core.common

import android.graphics.Paint

internal var Paint.opacity: Float
  get() = alpha / MAX_HEX_VALUE
  set(value) {
    alpha = (value * MAX_HEX_VALUE).toInt()
  }

internal fun Paint.withOpacity(opacity: Float, action: (Paint) -> Unit) {
  val previousOpacity = this.alpha
  color = color.copyColor(opacity * previousOpacity / MAX_HEX_VALUE)
  action(this)
  this.alpha = previousOpacity
}
