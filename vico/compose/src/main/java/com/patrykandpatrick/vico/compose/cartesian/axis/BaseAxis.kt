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

package com.patrykandpatrick.vico.compose.cartesian.axis

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.cartesian.axis.BaseAxis

/** Creates a [BaseAxis.Size.Auto] instance. */
public fun BaseAxis.Size.Companion.auto(
  min: Dp = 0.dp,
  max: Dp = Float.MAX_VALUE.dp,
): BaseAxis.Size.Auto = BaseAxis.Size.Auto(min.value, max.value)

/** Creates a [BaseAxis.Size.Fixed] instance. */
public fun BaseAxis.Size.Companion.fixed(value: Dp): BaseAxis.Size.Fixed =
  BaseAxis.Size.Fixed(value.value)

/** Creates a [BaseAxis.Size.Fraction] instance. */
public fun BaseAxis.Size.Companion.fraction(fraction: Float): BaseAxis.Size.Fraction =
  BaseAxis.Size.Fraction(fraction)

/** Creates a [BaseAxis.Size.Text] instance. */
public fun BaseAxis.Size.Companion.text(text: CharSequence): BaseAxis.Size.Text =
  BaseAxis.Size.Text(text)
