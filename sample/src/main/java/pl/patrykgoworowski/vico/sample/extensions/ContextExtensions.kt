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

package pl.patrykgoworowski.vico.sample.extensions

import android.content.Context
import android.util.TypedValue
import androidx.annotation.ColorInt
import pl.patrykgoworowski.vico.R
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.sample.component.getMarker

@ColorInt
internal fun Context.resolveColorAttribute(resourceId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(resourceId, typedValue, true)
    return typedValue.data
}

internal fun Context.getMarker(): Marker = getMarker(
    bubbleColor = resolveColorAttribute(resourceId = R.attr.colorSurface),
    indicatorInnerColor = resolveColorAttribute(resourceId = R.attr.colorSurface),
    guidelineColor = resolveColorAttribute(resourceId = R.attr.colorOnBackground),
)
