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

package pl.patrykgoworowski.vico.view.dimensions

import pl.patrykgoworowski.vico.core.dimensions.MutableDimensions

public fun dimensionsOf(allDp: Float): MutableDimensions = dimensionsOf(
    startDp = allDp,
    topDp = allDp,
    endDp = allDp,
    bottomDp = allDp,
)

public fun dimensionsOf(
    startDp: Float = 0f,
    topDp: Float = 0f,
    endDp: Float = 0f,
    bottomDp: Float = 0f,
): MutableDimensions = MutableDimensions(
    startDp = startDp,
    topDp = topDp,
    endDp = endDp,
    bottomDp = bottomDp,
)

public fun dimensionsOf(
    verticalDp: Float = 0f,
    horizontalDp: Float = 0f,
): MutableDimensions = MutableDimensions(
    startDp = horizontalDp,
    topDp = verticalDp,
    endDp = horizontalDp,
    bottomDp = verticalDp,
)
