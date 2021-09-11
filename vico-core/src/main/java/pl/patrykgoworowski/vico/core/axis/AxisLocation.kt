/*
 * Copyright (c) 2021. Patryk Goworowski
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

package pl.patrykgoworowski.vico.core.axis

sealed class AxisPosition {
    val isTop: Boolean
        get() = this is Horizontal.Top

    val isBottom: Boolean
        get() = this is Horizontal.Bottom

    val isStart: Boolean
        get() = this is Vertical.Start

    val isEnd: Boolean
        get() = this is Vertical.End

    fun isLeft(isLtr: Boolean): Boolean = this is Vertical.Start && isLtr

    fun isRight(isLtr: Boolean): Boolean = this is Vertical.End && isLtr

    sealed class Horizontal : AxisPosition() {
        object Top : Horizontal()
        object Bottom : Horizontal()
    }

    sealed class Vertical : AxisPosition() {
        object Start : Vertical()
        object End : Vertical()
    }
}