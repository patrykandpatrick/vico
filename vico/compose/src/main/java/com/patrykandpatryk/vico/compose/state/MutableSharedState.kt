/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.compose.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

public class MutableSharedState<P, T : P>(
    value: T,
    previousValue: P,
) : MutableState<T> {

    private val backingState = mutableStateOf(value)

    private var mutablePreviousValue: P = previousValue

    public val previousValue: P
        get() = mutablePreviousValue

    override var value: T
        get() = backingState.value
        set(value) {
            if (value !== this.value) {
                mutablePreviousValue = backingState.value
            }
            backingState.value = value
        }

    override fun component1(): T = backingState.component1()

    override fun component2(): (T) -> Unit = backingState.component2()
}

public fun <T> mutableSharedStateOf(value: T): MutableSharedState<T?, T> =
    MutableSharedState(
        value = value,
        previousValue = null,
    )

public fun <P, T : P & Any> mutableSharedStateOf(
    value: T,
    previousValue: P,
): MutableSharedState<P, T> =
    MutableSharedState(
        value = value,
        previousValue = previousValue,
    )
