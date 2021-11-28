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

public sealed class AxisPosition {

    public val isTop: Boolean
        get() = this is Horizontal.Top

    public val isBottom: Boolean
        get() = this is Horizontal.Bottom

    public val isStart: Boolean
        get() = this is Vertical.Start

    public val isEnd: Boolean
        get() = this is Vertical.End

    public fun isLeft(isLtr: Boolean): Boolean = this is Vertical.Start && isLtr

    public fun isRight(isLtr: Boolean): Boolean = this is Vertical.End && isLtr

    public sealed class Horizontal : AxisPosition() {
        public object Top : Horizontal()
        public object Bottom : Horizontal()
    }

    public sealed class Vertical : AxisPosition() {
        public object Start : Vertical()
        public object End : Vertical()
    }
}
