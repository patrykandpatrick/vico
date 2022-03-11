/*
 * Copyright (c) 2022. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.extension

/**
 * Calls the specified function block with [t1] and [t2] as its arguments if [t1] and [t2] are not null.
 * Returns `null` otherwise.
 */
public fun <T1, T2> ifNotNull(t1: T1?, t2: T2?, onNotNull: (T1, T2) -> Unit): Unit? =
    if (t1 != null && t2 != null) {
        onNotNull(t1, t2)
    } else {
        null
    }

/**
 * Calls the specified function block with [t1], [t2], and [t3] as its arguments if [t1], [t2], and [t3] are not null.
 * Returns `null` otherwise.
 */
public fun <T1, T2, T3> ifNotNull(t1: T1?, t2: T2?, t3: T3?, onNotNull: (T1, T2, T3) -> Unit): Unit? =
    if (t1 != null && t2 != null && t3 != null) {
        onNotNull(t1, t2, t3)
    } else {
        null
    }
