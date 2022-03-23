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

package pl.patrykgoworowski.vico.core.dimensions

/**
 * Creates a [MutableDimensions] instance using the sizes provided.
 */
public fun floatDimensions(
    startDp: Float = 0f,
    topDp: Float = 0f,
    endDp: Float = 0f,
    bottomDp: Float = 0f,
): MutableDimensions = MutableDimensions(startDp, topDp, endDp, bottomDp)
