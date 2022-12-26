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

package com.patrykandpatrick.vico.core.context

/**
 * Returns the value of the extra with the given [key] if such an extra exists. Otherwise, returns the result of
 * [block].
 */
public fun <T : Any> Extras.getExtraOr(
    key: Any,
    block: () -> T,
): T = if (hasExtra(key = key)) getExtra(key = key) else block()

/**
 * Returns the value of the extra with the given [key] if such an extra exists. Otherwise, returns the result of [block]
 * and saves it as an extra with the given key.
 */
public fun <T : Any> Extras.getOrPutExtra(
    key: Any,
    block: () -> T,
): T = getExtraOr(key = key) { block().also { putExtra(key = key, value = it) } }
