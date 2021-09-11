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

package pl.patrykgoworowski.vico.core.component.dimension

import pl.patrykgoworowski.vico.core.dimensions.MutableDimensions

public interface Padding {

    public val padding: MutableDimensions

    public fun setPadding(
        start: Float = 0f,
        top: Float = 0f,
        end: Float = 0f,
        bottom: Float = 0f,
    ) {
        padding.set(start, top, end, bottom)
    }

    public fun setPadding(
        horizontal: Float = 0f,
        vertical: Float = 0f,
    ) {
        padding.set(horizontal, vertical, horizontal, vertical)
    }

    public fun setPadding(
        all: Float = 0f
    ) {
        padding.set(all)
    }
}
