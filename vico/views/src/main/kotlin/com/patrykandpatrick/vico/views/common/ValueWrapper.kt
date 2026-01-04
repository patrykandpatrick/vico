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

package com.patrykandpatrick.vico.views.common

import kotlin.reflect.KProperty

internal class ValueWrapper<T>(var value: T)

internal operator fun <T> ValueWrapper<T>.getValue(thisObj: Any?, property: KProperty<*>): T = value

internal operator fun <T> ValueWrapper<T>.setValue(
  thisObj: Any?,
  property: KProperty<*>,
  value: T,
) {
  this.value = value
}

internal operator fun <T> ValueWrapper<T>.component1(): T = value

internal operator fun <T> ValueWrapper<T>.component2(): (T) -> Unit = { value = it }
