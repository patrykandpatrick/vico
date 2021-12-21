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

package pl.patrykgoworowski.vico.core.scroll

import pl.patrykgoworowski.vico.core.extension.between

public class ScrollHandler(
    private val setScrollAmount: (Float) -> Unit = {},
    public var maxScrollDistance: Float = 0f,
) {

    public var currentScroll: Float = 0f
        set(value) {
            field = getClampedScroll(value)
            setScrollAmount(value)
        }

    private fun getClampedScroll(scroll: Float): Float =
        minOf(scroll, maxScrollDistance).coerceAtLeast(0f)

    public fun handleScrollDelta(delta: Float): Float {
        currentScroll = getClampedScroll(currentScroll - delta)
        return (maxScrollDistance - currentScroll).between(0f, delta)
    }

    public fun handleScroll(targetScroll: Float): Float =
        handleScrollDelta(currentScroll - targetScroll)
}
