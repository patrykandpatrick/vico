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

package pl.patrykgoworowski.vico.core.scroll

/**
 * Handles scroll events.
 * @param maxScrollDistance the maximum scroll distance.
 */
public class ScrollHandler(
    private val setScrollAmount: (Float) -> Unit = {},
    public var maxScrollDistance: Float = 0f,
) {

    /**
     * The current scroll amount.
     */
    public var currentScroll: Float = 0f
        set(value) {
            field = getClampedScroll(value)
            setScrollAmount(value)
        }

    private fun getClampedScroll(scroll: Float): Float =
        minOf(scroll, maxScrollDistance).coerceAtLeast(0f)

    /**
     * Updates the [currentScroll] value by the given [delta] if the resulting scroll value is between 0 and the
     * [maxScrollDistance].
     */
    public fun handleScrollDelta(delta: Float): Float {
        val previousScroll = currentScroll
        currentScroll = getClampedScroll(currentScroll - delta)
        return previousScroll - currentScroll
    }

    /**
     * Checks whether a scroll by the given [delta] value is possible.
     */
    public fun canScrollBy(delta: Float): Boolean =
        delta == 0f || currentScroll - getClampedScroll(currentScroll - delta) != 0f

    /**
     * Scrolls to the [targetScroll] value, which is clamped to the range from 0 to the [maxScrollDistance].
     */
    public fun handleScroll(targetScroll: Float): Float =
        handleScrollDelta(currentScroll - targetScroll)
}
