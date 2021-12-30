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

package pl.patrykgoworowski.vico.core.context

public interface MeasureContext : Extras {
    public val density: Float
    public val fontScale: Float
    public val isLtr: Boolean
    public val isHorizontalScrollEnabled: Boolean
    public val zoom: Float

    public val Float.pixels: Float
        get() = this * density

    public val Float.wholePixels: Int
        get() = pixels.toInt()

    public fun toPixels(dp: Float): Float = dp * density
    public fun toFontSize(sp: Float): Float = sp * fontScale
}
