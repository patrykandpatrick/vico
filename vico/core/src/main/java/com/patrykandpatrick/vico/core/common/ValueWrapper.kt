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

import androidx.annotation.RestrictTo
import kotlin.reflect.KProperty

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) public class ValueWrapper<T>(public var value: T)

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public operator fun <T> ValueWrapper<T>.getValue(thisObj: Any?, property: KProperty<*>): T = value

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public operator fun <T> ValueWrapper<T>.setValue(thisObj: Any?, property: KProperty<*>, value: T) {
  this.value = value
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public operator fun <T> ValueWrapper<T>.component1(): T = value

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public operator fun <T> ValueWrapper<T>.component2(): (T) -> Unit = { value = it }
